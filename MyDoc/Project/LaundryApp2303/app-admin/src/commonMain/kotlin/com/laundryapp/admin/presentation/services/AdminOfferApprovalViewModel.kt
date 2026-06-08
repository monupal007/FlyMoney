package com.laundryapp.admin.presentation.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Offer
import com.laundryapp.core.data.model.OfferType
import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.data.remote.Message
import com.laundryapp.core.data.remote.OpenRouterApi
import com.laundryapp.core.data.remote.OpenRouterRequest
import com.laundryapp.core.data.repository.ServiceRepository
import com.laundryapp.core.data.repository.VendorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OfferWithVendor(
    val offer: Offer,
    val vendorName: String
)

class AdminOfferApprovalViewModel(
    private val serviceRepository: ServiceRepository,
    private val vendorRepository: VendorRepository,
    private val openRouterApi: OpenRouterApi
) : ViewModel() {
    private val _allOffers = MutableStateFlow<List<Offer>>(emptyList())
    private val _allVendors = MutableStateFlow<List<Vendor>>(emptyList())
    
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val OPENROUTER_API_KEY = "Bearer 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"
    private val MODEL_NAME = "google/gemini-2.0-flash-001"

    val pendingOffers: StateFlow<List<OfferWithVendor>> = combine(_allOffers, _allVendors) { offers, vendors ->
        offers.filter { !it.approved && !it.rejected }.map { offer ->
            val vendor = vendors.find { it.vendorId == offer.vendorId }
            OfferWithVendor(
                offer = offer,
                vendorName = vendor?.shopName ?: "Unknown Vendor"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val approvedOffers: StateFlow<List<OfferWithVendor>> = combine(_allOffers, _allVendors) { offers, vendors ->
        offers.filter { it.approved }.map { offer ->
            val vendor = vendors.find { it.vendorId == offer.vendorId }
            OfferWithVendor(
                offer = offer,
                vendorName = if (offer.vendorId.isEmpty()) "Admin (Global)" else (vendor?.shopName ?: "Unknown Vendor")
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val rejectedOffers: StateFlow<List<OfferWithVendor>> = combine(_allOffers, _allVendors) { offers, vendors ->
        offers.filter { it.rejected }.map { offer ->
            val vendor = vendors.find { it.vendorId == offer.vendorId }
            OfferWithVendor(
                offer = offer,
                vendorName = vendor?.shopName ?: "Unknown Vendor"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                serviceRepository.getOffers().collect { _allOffers.value = it }
            }
            launch {
                vendorRepository.getAllVendors().collect { _allVendors.value = it }
            }
        }
    }

    fun approveOffer(offerId: String) {
        viewModelScope.launch {
            serviceRepository.approveOffer(offerId)
        }
    }

    fun approveAllPendingOffers() {
        viewModelScope.launch {
            pendingOffers.value.forEach { 
                serviceRepository.approveOffer(it.offer.id)
            }
        }
    }

    fun rejectOffer(offerId: String) {
        viewModelScope.launch {
            serviceRepository.rejectOffer(offerId)
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            serviceRepository.deleteOffer(offerId)
                .onSuccess { _uiEvent.emit("Offer deleted successfully") }
                .onFailure { _uiEvent.emit("Failed to delete offer: ${it.message}") }
        }
    }

    fun generateDescription(title: String, code: String, value: String, type: OfferType, minOrder: String, onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val discountText = if (type == OfferType.FLAT) "₹$value OFF" else "$value% OFF"
                val prompt = """
                    Write a catchy, high-conversion 2-line marketing punchline for a laundry service offer.
                    Offer Name: $title
                    Coupon Code: $code
                    Benefit: $discountText on orders over ₹$minOrder
                    Style: Exciting, urgent, and persuasive.
                    Constraint: Max 80 characters. Include one relevant emoji.
                    Output: Return ONLY the punchline text.
                """.trimIndent()
                
                val request = OpenRouterRequest(
                    model = MODEL_NAME,
                    messages = listOf(Message(role = "user", content = prompt))
                )
                
                openRouterApi.generateCompletion(OPENROUTER_API_KEY, request = request)
                    .onSuccess { response ->
                        val description = response.choices.firstOrNull()?.message?.content?.trim()
                        if (description != null) {
                            onGenerated(description.replace("\"", ""))
                        } else {
                            _uiEvent.emit("Empty response from AI")
                        }
                    }
                    .onFailure { error ->
                        _uiEvent.emit("AI Error: ${error.message}")
                    }
            } catch (e: Exception) {
                _uiEvent.emit("Error: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun createAdminOffer(offer: Offer) {
        viewModelScope.launch {
            val adminOffer = offer.copy(
                vendorId = "",
                approved = true,
                active = true,
                rejected = false
            )
            serviceRepository.addOffer(adminOffer)
                .onSuccess { _uiEvent.emit("Global offer created successfully") }
                .onFailure { _uiEvent.emit("Failed to create offer: ${it.message}") }
        }
    }
}
