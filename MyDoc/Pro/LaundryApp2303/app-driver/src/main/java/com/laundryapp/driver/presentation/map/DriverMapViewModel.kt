package com.laundryapp.driver.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.laundryapp.core.data.model.LatLngData
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.repository.DriverRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.util.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DriverMapViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val driverRepository: DriverRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _driverLocation = MutableStateFlow<LatLngData?>(null)
    val driverLocation = _driverLocation.asStateFlow()

    private val _nearbyTasks = MutableStateFlow<List<Order>>(emptyList())
    val nearbyTasks = _nearbyTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observeDriverLocation()
        observeNearbyTasks()
    }

    private fun observeDriverLocation() {
        val driverId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            driverRepository.getDriverFlow(driverId).collect { driver ->
                _driverLocation.value = driver?.location
            }
        }
    }

    private fun observeNearbyTasks() {
        combine(orderRepository.getUnassignedTasks(), _driverLocation) { tasks, location ->
            if (location == null) return@combine emptyList<Order>()
            tasks.filter { task ->
                val taskLoc = LatLngData(task.pickupAddress.latitude, task.pickupAddress.longitude)
                LocationUtils.calculateDistance(
                    location.lat, location.lng,
                    taskLoc.lat, taskLoc.lng
                ) <= 5.0 // 5km Geofence
            }
        }.onEach {
            _nearbyTasks.value = it
        }.launchIn(viewModelScope)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: Context) {
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (location != null) {
                    val latLng = LatLngData(location.latitude, location.longitude)
                    _driverLocation.value = latLng
                    updateLocation(latLng.lat, latLng.lng)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateLocation(lat: Double, lng: Double) {
        val driverId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            driverRepository.updateLocation(driverId, lat, lng)
        }
    }
}
