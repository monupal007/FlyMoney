package com.laundryapp.customer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.*
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.ServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val defaultAddress: Address? = null,
    val hasAddresses: Boolean = true,
    val services: List<LaundryService> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
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
                .collect { offers ->
                    _uiState.update { it.copy(offers = offers.filter { it.isApproved && it.isActive }, isLoading = false) }
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
        // userId logic needs to be platform independent
        // This is a simplified version
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            // Simplified for now, we need to add getUserProfileFlow to KMP repository
            authRepository.getUserProfile(userId).onSuccess { user ->
                val defaultAddr = user.addresses.find { it.isDefault } ?: user.addresses.firstOrNull()
                _uiState.update { it.copy(
                    user = user,
                    defaultAddress = defaultAddr,
                    hasAddresses = user.addresses.isNotEmpty(),
                    isLoading = false
                ) }
            }.onFailure { 
                 _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
