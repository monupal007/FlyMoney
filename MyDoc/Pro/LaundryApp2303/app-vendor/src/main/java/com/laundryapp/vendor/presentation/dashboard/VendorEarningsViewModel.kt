package com.laundryapp.vendor.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VendorEarningsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState = _uiState.asStateFlow()

    private var allOrders: List<Order> = emptyList()

    init {
        loadEarnings()
    }

    private fun loadEarnings() {
        viewModelScope.launch {
            val vendorId = authRepository.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            
            orderRepository.getVendorOrders(vendorId).collect { orders ->
                allOrders = orders.filter { it.status == OrderStatus.DELIVERED }
                updateStateWithFilter(_uiState.value.filter)
            }
        }
    }

    fun setFilter(filter: EarningsFilter) {
        updateStateWithFilter(filter)
    }

    private fun updateStateWithFilter(filter: EarningsFilter) {
        val calendar = Calendar.getInstance()
        
        val filteredOrders = when (filter) {
            EarningsFilter.LAST_7_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                allOrders.filter { it.createdAt >= calendar.timeInMillis }
            }
            EarningsFilter.LAST_30_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                allOrders.filter { it.createdAt >= calendar.timeInMillis }
            }
        }

        // Use the vendorEarning field directly as per requirements
        val total = filteredOrders.sumOf { it.vendorEarning }
        val avg = if (filteredOrders.isNotEmpty()) total / filteredOrders.size else 0.0
        
        val groupedPoints = groupOrdersForChart(filteredOrders, filter)
        
        // Map recent orders to activities
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val recentActivities = allOrders.sortedByDescending { it.createdAt }
            .take(10) // Show last 10 activities
            .map { order ->
                RecentActivity(
                    id = order.id,
                    title = "Order #${order.id.takeLast(6).uppercase()}",
                    date = dateFormat.format(Date(order.createdAt)),
                    amount = order.vendorEarning,
                    type = ActivityType.ORDER
                )
            }
        
        _uiState.update { it.copy(
            totalEarnings = total,
            averageEarnings = avg,
            orderCount = filteredOrders.size,
            points = groupedPoints,
            recentActivities = recentActivities,
            filter = filter,
            isLoading = false
        ) }
    }

    private fun groupOrdersForChart(orders: List<Order>, filter: EarningsFilter): List<EarningPoint> {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val points = mutableListOf<EarningPoint>()

        val days = if (filter == EarningsFilter.LAST_7_DAYS) 7 else 30
        for (i in days - 1 downTo 0) {
            val dayCalendar = Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCalendar.timeInMillis
            val end = Calendar.getInstance().apply { 
                timeInMillis = start
                add(Calendar.DAY_OF_YEAR, 1) 
            }.timeInMillis
            
            val amount = orders.filter { it.createdAt in start until end }.sumOf { it.vendorEarning }
            points.add(EarningPoint(dateFormat.format(Date(start)), amount, start))
        }
        return points
    }
}
