package com.laundryapp.core.presentation.driver.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.*
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.DriverRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.VendorRepository
import com.laundryapp.core.util.LocationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DriverTask(
    val id: String, // orderId + type
    val order: Order,
    val isPickupToVendor: Boolean,
    val isCompleted: Boolean
)

class DriverDashboardViewModel(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val driverRepository: DriverRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {
    private val _activeTasks = MutableStateFlow<List<DriverTask>>(emptyList())
    val activeTasks = _activeTasks.asStateFlow()

    private val _completedTasks = MutableStateFlow<List<DriverTask>>(emptyList())
    val completedTasks = _completedTasks.asStateFlow()

    val totalEarnings: StateFlow<Double> = _completedTasks.map { tasks ->
        tasks.sumOf { if (it.isPickupToVendor) it.order.pickupFee else it.order.deliveryFee }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _unassignedTasks = MutableStateFlow<List<Order>>(emptyList())
    val unassignedTasks = _unassignedTasks.asStateFlow()

    private val _ignoredOrderIds = MutableStateFlow<Set<String>>(emptySet())

    private val _vendorLocations = MutableStateFlow<Map<String, LatLngData>>(emptyMap())
    val vendorLocations = _vendorLocations.asStateFlow()

    private val _driverLocation = MutableStateFlow<LatLngData?>(null)
    val driverLocation = _driverLocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private var firstLoad = true
    private var dataCollectionJob: Job? = null

    init {
        observeAuthState()
        loadVendorLocations()
    }

    private fun loadVendorLocations() {
        viewModelScope.launch {
            vendorRepository.getAllVendors().collect { vendors ->
                _vendorLocations.value = vendors.associate { it.vendorId to it.location }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                if (user != null) {
                    startDataCollection(user.uid)
                } else {
                    stopDataCollection()
                }
            }
        }
    }

    private fun startDataCollection(driverId: String) {
        dataCollectionJob?.cancel()
        dataCollectionJob = viewModelScope.launch {
            _isLoading.value = true
            
            // Monitor online status and location
            launch {
                driverRepository.getDriverFlow(driverId).collect { driver ->
                    val newStatus = driver?.isOnline ?: false
                    if (!firstLoad && _isOnline.value != newStatus) {
                        _message.value = if (newStatus) "You are now online" else "You are now offline"
                    }
                    _isOnline.value = newStatus
                    _driverLocation.value = driver?.location
                    
                    if (!newStatus) {
                        _unassignedTasks.value = emptyList()
                    }
                    firstLoad = false
                }
            }

            // Monitor driver tasks (both active and completed)
            launch {
                orderRepository.getDriverOrders(driverId).collect { orders ->
                    val allTasks = mutableListOf<DriverTask>()
                    
                    orders.forEach { order ->
                        // Check for Pickup Task
                        if (order.pickupDriverId == driverId) {
                            val isActive = order.status in listOf(
                                OrderStatus.PICKUP_ASSIGNED, 
                                OrderStatus.PICKED_UP_FROM_CUSTOMER, 
                                OrderStatus.PICKED_UP
                            )
                            val isCompleted = order.status !in listOf(
                                OrderStatus.PLACED,
                                OrderStatus.CONFIRMED,
                                OrderStatus.ACCEPTED_BY_VENDOR,
                                OrderStatus.PICKUP_ASSIGNED, 
                                OrderStatus.PICKED_UP_FROM_CUSTOMER, 
                                OrderStatus.PICKED_UP
                            )
                            
                            if (isActive || isCompleted) {
                                allTasks.add(DriverTask(
                                    id = "${order.id}_pickup",
                                    order = order,
                                    isPickupToVendor = true,
                                    isCompleted = isCompleted
                                ))
                            }
                        }
                        
                        // Check for Delivery Task
                        if (order.deliveryDriverId == driverId) {
                            val isActive = order.status in listOf(
                                OrderStatus.DELIVERY_ASSIGNED, 
                                OrderStatus.PICKED_UP_FROM_VENDOR, 
                                OrderStatus.OUT_FOR_DELIVERY
                            )
                            val isCompleted = order.status == OrderStatus.DELIVERED
                            
                            if (isActive || isCompleted) {
                                allTasks.add(DriverTask(
                                    id = "${order.id}_delivery",
                                    order = order,
                                    isPickupToVendor = false,
                                    isCompleted = isCompleted
                                ))
                            }
                        }
                    }

                    _activeTasks.value = allTasks.filter { !it.isCompleted }
                    _completedTasks.value = allTasks.filter { it.isCompleted }.sortedByDescending { it.order.updatedAt }
                }
            }

            launch {
                combine(
                    _isOnline, 
                    orderRepository.getUnassignedTasks(), 
                    _ignoredOrderIds, 
                    _driverLocation,
                    _vendorLocations
                ) { isOnline, tasks, ignored, location, vendors ->
                    if (isOnline && location != null) {
                        tasks.filter { task ->
                            // 1. Not ignored
                            if (task.id in ignored) return@filter false
                            
                            // 2. Identify target location (Pickup point)
                            val isPickupToVendor = task.status == OrderStatus.ACCEPTED_BY_VENDOR
                            val targetLocation = if (isPickupToVendor) {
                                LatLngData(task.pickupAddress.latitude, task.pickupAddress.longitude)
                            } else {
                                vendors[task.vendorId]
                            }
                            
                            // 3. 5km Geofence Check
                            if (targetLocation != null) {
                                LocationUtils.calculateDistance(
                                    location.lat, location.lng,
                                    targetLocation.lat, targetLocation.lng
                                ) <= 5.0
                            } else false
                        }
                    } else {
                        emptyList()
                    }
                }.collect {
                    _unassignedTasks.value = it
                }
            }
            
            _isLoading.value = false
        }
    }

    private fun stopDataCollection() {
        dataCollectionJob?.cancel()
        dataCollectionJob = null
        _activeTasks.value = emptyList()
        _completedTasks.value = emptyList()
        _unassignedTasks.value = emptyList()
        _isOnline.value = false
        _driverLocation.value = null
        _ignoredOrderIds.value = emptySet()
    }

    fun updateLocation(lat: Double, lng: Double) {
        val driverId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            driverRepository.updateLocation(driverId, lat, lng)
        }
    }

    fun acceptTask(order: Order) {
        if (!_isOnline.value) {
            _message.value = "You must be online to accept tasks"
            return
        }
        val driverId = authRepository.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            val driver = driverRepository.getDriver(driverId).getOrNull()
            val driverName = driver?.name ?: authRepository.currentUser?.displayName ?: "Driver"
            val driverPhone = driver?.phone ?: ""
            
            val isPickup = order.status == OrderStatus.ACCEPTED_BY_VENDOR

            orderRepository.assignDriver(order.id, driverId, driverName, isPickup, driverPhone)
                .onSuccess { _message.value = "Task accepted successfully" }
                .onFailure { _message.value = "Failed to accept task: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun rejectTask(orderId: String) {
        _ignoredOrderIds.update { it + orderId }
        _message.value = "Task rejected"
    }

    fun clearMessage() {
        _message.value = null
    }
}
