package com.laundryapp.vendor.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Offer
import com.laundryapp.core.data.model.OfferType
import com.laundryapp.core.data.remote.Message
import com.laundryapp.core.data.remote.OpenRouterApi
import com.laundryapp.core.data.remote.OpenRouterRequest
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorOfferViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    private val openRouterApi: OpenRouterApi
) : ViewModel() {
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers = _offers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val OPENROUTER_API_KEY = "Bearer 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"
    private val MODEL_NAME = "google/gemini-2.0-flash-001" 

    init {
        loadVendorOffers()
    }

    private fun loadVendorOffers() {
        val vendorId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            serviceRepository.getOffers().collectLatest { allOffers ->
                // Filter for vendor's own offers AND global/admin offers
                _offers.value = allOffers.filter { 
                    it.vendorId == vendorId || (it.vendorId.isEmpty() && it.approved && it.active)
                }
                _isLoading.value = false
            }
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
                
                val response = openRouterApi.generateCompletion(OPENROUTER_API_KEY, request = request)
                if (response.isSuccessful) {
                    val description = response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                    if (description != null) {
                        onGenerated(description.replace("\"", ""))
                    } else {
                        _uiEvent.emit("Empty response from AI")
                    }
                } else {
                    _uiEvent.emit("AI Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiEvent.emit("Error: ${e.localizedMessage}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            val offerToDelete = _offers.value.find { it.id == offerId }
            if (offerToDelete != null && offerToDelete.vendorId.isEmpty()) {
                _uiEvent.emit("Admin offers cannot be deleted by vendors")
                return@launch
            }

            serviceRepository.deleteOffer(offerId).onSuccess {
                _uiEvent.emit("Offer deleted successfully")
            }.onFailure {
                _uiEvent.emit("Error deleting offer: ${it.message}")
            }
        }
    }

    fun createOffer(offer: Offer) {
        val vendorId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            serviceRepository.addOffer(offer.copy(vendorId = vendorId, approved = false, active = false, rejected = false))
                .onSuccess {
                    _uiEvent.emit("Offer submitted for approval")
                    _isLoading.value = false
                }
                .onFailure {
                    _uiEvent.emit("Error creating offer: ${it.message}")
                    _isLoading.value = false
                }
        }
    }
}
