package com.laundryapp.core.presentation.driver.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.VendorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val orderRepository: OrderRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order = _order.asStateFlow()

    private val _vendor = MutableStateFlow<Vendor?>(null)
    val vendor = _vendor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _isUpdateFinished = MutableSharedFlow<Unit>()
    val isUpdateFinished = _isUpdateFinished.asSharedFlow()

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.getOrderById(orderId).collect { ord ->
                _order.value = ord
                if (ord != null && ord.vendorId.isNotEmpty()) {
                    vendorRepository.getVendor(ord.vendorId).onSuccess {
                        _vendor.value = it
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess {
                    if (status == OrderStatus.RECEIVED_BY_VENDOR || status == OrderStatus.DELIVERED) {
                        _isUpdateFinished.emit(Unit)
                    }
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _message.value = "Failed to update: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
