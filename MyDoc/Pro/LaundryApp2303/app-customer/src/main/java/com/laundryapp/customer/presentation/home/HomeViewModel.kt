package com.laundryapp.customer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.*
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val defaultAddress: Address? = null,
    val hasAddresses: Boolean = true, // Default to true to avoid flicker
    val services: List<LaundryService> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeUserProfile()
    }

    private fun loadHomeData() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            serviceRepository.getServices()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { services ->
                    _uiState.update { it.copy(services = services) }
                }
        }

        viewModelScope.launch {
            serviceRepository.getOffers()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .map { offers -> 
                    // Only approved and active offers are visible to customers
                    offers.filter { it.isApproved && it.isActive } 
                }
                .collect { offers ->
                    _uiState.update { it.copy(offers = offers, isLoading = false) }
                }
        }

        viewModelScope.launch {
            orderRepository.getCustomerOrders(userId)
                .map { orders -> orders.take(3) }
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { recentOrders ->
                    _uiState.update { it.copy(recentOrders = recentOrders) }
                }
        }
    }

    private fun observeUserProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            authRepository.getUserProfileFlow(userId, UserRole.CUSTOMER).collect { user ->
                if (user != null) {
                    val defaultAddr = user.addresses.find { it.isDefault } ?: user.addresses.firstOrNull()
                    _uiState.update { it.copy(
                        user = user,
                        defaultAddress = defaultAddr,
                        hasAddresses = user.addresses.isNotEmpty(),
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
