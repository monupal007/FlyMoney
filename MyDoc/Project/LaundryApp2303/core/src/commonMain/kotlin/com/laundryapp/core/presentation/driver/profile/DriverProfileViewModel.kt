package com.laundryapp.core.presentation.driver.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Driver
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.DriverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DriverProfileViewModel(
    private val authRepository: AuthRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _driver = MutableStateFlow<Driver?>(null)
    val driver = _driver.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadDriverProfile()
    }

    private fun loadDriverProfile() {
        val driverId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            driverRepository.getDriverFlow(driverId).collect {
                _driver.value = it
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }

    fun toggleOnlineStatus(isOnline: Boolean) {
        val driverId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            driverRepository.updateDriverStatus(driverId, isOnline).onSuccess {
                _driver.update { it?.copy(isOnline = isOnline) }
            }
        }
    }
}
