package com.laundryapp.admin.presentation.vendors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VendorDetailViewModel(
    private val vendorRepository: VendorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _vendor = MutableStateFlow<Vendor?>(null)
    val vendor = _vendor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadVendor(vendorId: String) {
        if (vendorId.isBlank()) {
            _error.value = "Invalid Vendor ID"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            vendorRepository.getVendor(vendorId).onSuccess {
                if (it == null) {
                    _error.value = "Vendor not found in database"
                }
                _vendor.value = it
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to load vendor details"
            }
            _isLoading.value = false
        }
    }

    fun approveVendor(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.approveVendor(vendorId).onSuccess {
                loadVendor(vendorId)
            }.onFailure { e ->
                _error.value = "Failed to approve: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun rejectVendor(vendorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.rejectVendor(vendorId).onSuccess {
                loadVendor(vendorId)
            }.onFailure { e ->
                _error.value = "Failed to reject: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}
