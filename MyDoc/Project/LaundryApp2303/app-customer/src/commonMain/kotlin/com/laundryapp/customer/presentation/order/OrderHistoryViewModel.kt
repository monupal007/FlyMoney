package com.laundryapp.customer.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class OrderHistoryViewModel(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            orderRepository.getCustomerOrders(userId)
                .collect { orders ->
                    _uiState.update { it.copy(orders = orders, isLoading = false) }
                }
        }
    }
}
