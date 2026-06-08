package com.laundryapp.admin.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Driver
import com.laundryapp.core.data.repository.DriverRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DriverManagementViewModel(
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _allDrivers = MutableStateFlow<List<Driver>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Pending, 1: Approved, 2: All
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val filteredDrivers = combine(_allDrivers, _searchQuery, _selectedTab) { drivers, query, tab ->
        drivers.filter { driver ->
            val matchesSearch = driver.name.contains(query, ignoreCase = true) || 
                              driver.vehicleNumber.contains(query, ignoreCase = true)
            val matchesTab = when (tab) {
                0 -> !driver.isApproved
                1 -> driver.isApproved
                else -> true
            }
            matchesSearch && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDrivers()
    }

    fun loadDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            driverRepository.getAllDrivers()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { 
                    _allDrivers.value = it
                    _isLoading.value = false
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun approveDriver(driverId: String) {
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("drivers").document(driverId).update("isApproved" to true)
                // Flow will automatically update the UI
            } catch (e: Exception) {
                _error.value = "Failed to approve: ${e.message}"
            }
        }
    }

    fun rejectDriver(driverId: String) {
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("drivers").document(driverId).delete()
                // Flow will automatically update the UI
            } catch (e: Exception) {
                _error.value = "Failed to reject: ${e.message}"
            }
        }
    }
}
