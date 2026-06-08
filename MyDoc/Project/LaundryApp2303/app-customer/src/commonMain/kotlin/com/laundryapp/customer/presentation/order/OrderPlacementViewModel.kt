package com.laundryapp.customer.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.*
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.ServiceRepository
import com.laundryapp.core.data.repository.VendorRepository
import com.laundryapp.core.util.PaymentCallback
import com.laundryapp.core.util.PaymentGateway
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderPlacementUiState(
    val services: List<LaundryService> = emptyList(),
    val vendors: List<Vendor> = emptyList(),
    val selectedVendor: Vendor? = null,
    val selectedItems: Map<String, Map<String, Double>> = emptyMap(),
    val pickupAddress: Address? = null,
    val deliveryAddress: Address? = null,
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val orderPlacedId: String? = null
)

class OrderPlacementViewModel(
    private val authRepository: AuthRepository,
    private val serviceRepository: ServiceRepository,
    private val vendorRepository: VendorRepository,
    private val orderRepository: OrderRepository,
    private val paymentGateway: PaymentGateway
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderPlacementUiState())
    val uiState: StateFlow<OrderPlacementUiState> = _uiState.asStateFlow()

    init {
        loadServices()
        observeUserProfile()
    }

    private fun loadServices() {
        viewModelScope.launch {
            serviceRepository.getServices().collect { services ->
                _uiState.update { it.copy(services = services) }
            }
        }
    }

    private fun observeUserProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            authRepository.getUserProfile(userId).onSuccess { user ->
                val defaultAddr = user.addresses.find { it.isDefault } ?: user.addresses.firstOrNull()
                _uiState.update { it.copy(pickupAddress = defaultAddr, deliveryAddress = defaultAddr) }
            }
        }
    }

    fun placeOrder() {
        val userId = authRepository.currentUser?.uid ?: return
        val currentState = _uiState.value
        
        // Simplified order placement with payment gateway trigger
        paymentGateway.startPayment(
            amount = currentState.totalAmount,
            orderId = "temp_${System.currentTimeMillis()}",
            customerEmail = "", // Get from user
            customerPhone = currentState.pickupAddress?.mobileNumber ?: "",
            callback = object : PaymentCallback {
                override fun onSuccess(paymentId: String) {
                    // Create and save order in Firestore
                }

                override fun onError(code: Int, message: String) {
                    _uiState.update { it.copy(error = message) }
                }
            }
        )
    }
}
