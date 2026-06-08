package com.laundryapp.driver.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideAlertViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        object Success : UiEvent()
        data class Error(val message: String) : UiEvent()
    }

    fun acceptRide(orderId: String) {
        viewModelScope.launch {
            val firebaseUser = authRepository.currentUser
            if (firebaseUser != null) {
                val order = orderRepository.getOrderById(orderId).firstOrNull()
                if (order != null) {
                    val isPickup = order.pickupDriverId.isEmpty() && order.pickupDriverName.isEmpty()
                    
                    val result = orderRepository.assignDriver(
                        orderId = orderId,
                        driverId = firebaseUser.uid,
                        driverName = firebaseUser.displayName ?: "Driver",
                        isPickup = isPickup,
                        driverPhone = firebaseUser.phoneNumber ?: ""
                    )
                    
                    if (result.isSuccess) {
                        // Notify Customer that driver has been assigned
                        notificationRepository.notifyCustomerStatusUpdate(
                            userId = order.userId,
                            orderId = orderId,
                            status = if (isPickup) "PICKUP_ASSIGNED" else "DELIVERY_ASSIGNED"
                        )
                        
                        _eventFlow.emit(UiEvent.Success)
                    } else {
                        _eventFlow.emit(UiEvent.Error(result.exceptionOrNull()?.message ?: "Failed to accept ride"))
                    }
                } else {
                    _eventFlow.emit(UiEvent.Error("Order not found"))
                }
            } else {
                _eventFlow.emit(UiEvent.Error("Driver not authenticated"))
            }
        }
    }

    fun rejectRide() {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.Success)
        }
    }
}
