package com.laundryapp.admin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Driver
import com.laundryapp.core.data.model.Offer
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.data.repository.DriverRepository
import com.laundryapp.core.data.repository.OrderRepository
import com.laundryapp.core.data.repository.ServiceRepository
import com.laundryapp.core.data.repository.VendorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalOrders: Int = 0,
    val activeOrders: Int = 0,
    val totalVendors: Int = 0,
    val pendingVendors: Int = 0,
    val totalDrivers: Int = 0,
    val onlineDrivers: Int = 0,
    val pendingDrivers: Int = 0,
    val pendingOffers: Int = 0,
    val totalRevenue: Double = 0.0
)

class AdminDashboardViewModel(
    private val orderRepository: OrderRepository,
    private val vendorRepository: VendorRepository,
    private val serviceRepository: ServiceRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {
    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _allVendors = MutableStateFlow<List<Vendor>>(emptyList())
    private val _allOffers = MutableStateFlow<List<Offer>>(emptyList())
    private val _allDrivers = MutableStateFlow<List<Driver>>(emptyList())
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val stats: StateFlow<DashboardStats> = combine(
        _allOrders, _allVendors, _allOffers, _allDrivers
    ) { orders, vendors, offers, drivers ->
        DashboardStats(
            totalOrders = orders.size,
            activeOrders = orders.count { it.status !in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED) },
            totalRevenue = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.adminEarning },
            totalVendors = vendors.size,
            pendingVendors = vendors.count { !it.isApproved && !it.isRejected },
            totalDrivers = drivers.size,
            onlineDrivers = drivers.count { it.isOnline },
            pendingDrivers = drivers.count { !it.isApproved && !it.isRejected },
            pendingOffers = offers.count { !it.approved && !it.rejected }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), DashboardStats())

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            
            try {
                launch {
                    orderRepository.getAllOrders()
                        .catch { e -> _errorMessage.value = "Orders Error: ${e.message}" }
                        .collect { _allOrders.value = it }
                }
                
                launch {
                    vendorRepository.getAllVendors()
                        .catch { e -> _errorMessage.value = "Vendors Error: ${e.message}" }
                        .collect { _allVendors.value = it }
                }

                launch {
                    serviceRepository.getOffers()
                        .catch { e -> _errorMessage.value = "Offers Error: ${e.message}" }
                        .collect { _allOffers.value = it }
                }

                launch {
                    driverRepository.getAllDrivers()
                        .catch { e -> _errorMessage.value = "Drivers Error: ${e.message}" }
                        .collect { 
                            _allDrivers.value = it
                            _isRefreshing.value = false
                        }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isRefreshing.value = false
            }
        }
    }
}
