package com.laundryapp.admin.presentation.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.LaundryItem
import com.laundryapp.core.data.model.LaundryService
import com.laundryapp.core.data.repository.ServiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServiceManagementViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _services = MutableStateFlow<List<LaundryService>>(emptyList())
    val services: StateFlow<List<LaundryService>> = _services.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            serviceRepository.getServices()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect {
                    _services.value = it
                    _isLoading.value = false
                }
        }
    }

    fun addService(name: String, description: String, category: String, iconUrl: String) {
        viewModelScope.launch {
            val service = LaundryService(
                name = name,
                description = description,
                category = category,
                iconUrl = iconUrl
            )
            serviceRepository.addService(service)
                .onFailure { _error.value = it.message }
        }
    }

    fun updateService(service: LaundryService) {
        viewModelScope.launch {
            serviceRepository.updateService(service)
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            serviceRepository.deleteService(serviceId)
                .onFailure { _error.value = it.message }
        }
    }

    fun addLaundryItem(serviceId: String, name: String, basePrice: Double, iconUrl: String) {
        viewModelScope.launch {
            val item = LaundryItem(
                name = name,
                basePrice = basePrice,
                iconUrl = iconUrl
            )
            serviceRepository.addLaundryItem(serviceId, item)
                .onFailure { _error.value = it.message }
        }
    }
    
    fun getLaundryItems(serviceId: String): Flow<List<LaundryItem>> {
        return serviceRepository.getLaundryItems(serviceId)
    }

    fun updateLaundryItem(serviceId: String, item: LaundryItem) {
        viewModelScope.launch {
            serviceRepository.updateLaundryItem(serviceId, item)
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteLaundryItem(serviceId: String, itemId: String) {
        viewModelScope.launch {
            serviceRepository.deleteLaundryItem(serviceId, itemId)
                .onFailure { _error.value = it.message }
        }
    }
}
