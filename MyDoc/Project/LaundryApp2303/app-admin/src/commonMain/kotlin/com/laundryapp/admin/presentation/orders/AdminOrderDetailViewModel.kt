package com.laundryapp.admin.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Driver
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.data.repository.DriverRepository
import com.laundryapp.core.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminOrderDetailViewModel(
    private val orderRepository: OrderRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        loadDrivers()
    }

    private fun loadDrivers() {
        viewModelScope.launch {
            _drivers.value = driverRepository.getApprovedDrivers()
        }
    }

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.getOrderById(orderId).collect {
                _order.value = it
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
                .onFailure { _message.value = "Failed to update status: ${it.message}" }
        }
    }

    fun assignDriver(orderId: String, driverId: String, isPickup: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val driver = _drivers.value.find { it.driverId == driverId }
            val driverName = driver?.name ?: "Unknown Driver"
            val driverPhone = driver?.phone ?: ""
            orderRepository.assignDriver(orderId, driverId, driverName, isPickup, driverPhone)
                .onSuccess { _message.value = "Driver assigned successfully" }
                .onFailure { _message.value = "Failed to assign driver: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
