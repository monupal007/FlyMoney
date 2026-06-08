package com.laundryapp.admin.presentation.vendors

import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.VendorRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VendorManagementViewModel(
    private val authRepository: AuthRepository,
    private val vendorRepository: VendorRepository
) : ViewModel() {
    private val _allVendors = MutableStateFlow<List<Vendor>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Pending, 1: Approved, 2: All
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val filteredVendors = combine(_allVendors, _searchQuery, _selectedTab) { vendors, query, tab ->
        vendors.filter { vendor ->
            val matchesQuery = vendor.shopName.contains(query, ignoreCase = true) || 
                               vendor.ownerName.contains(query, ignoreCase = true)
            val matchesTab = when (tab) {
                0 -> !vendor.isApproved && !vendor.isRejected
                1 -> vendor.isApproved
                2 -> true
                else -> true
            }
            matchesQuery && matchesTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadVendors()
    }

    fun loadVendors() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            vendorRepository.getAllVendors()
                .catch { e ->
                    _error.value = "Failed to load vendors: ${e.message}"
                    _isLoading.value = false
                }
                .collect { 
                    _allVendors.value = it
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

    fun approveVendor(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.approveVendor(vendorId)
                .onSuccess {
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Approval failed: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun rejectVendor(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.rejectVendor(vendorId)
                .onSuccess {
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Rejection failed: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
}
