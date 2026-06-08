package com.laundryapp.customer.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Address
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.util.AddressDetails
import com.laundryapp.core.util.LatLng
import com.laundryapp.core.util.platformLocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddressUiState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddSheet: Boolean = false,
    val mapLocation: LatLng = LatLng(20.5937, 78.9629), // India center
    val isFetchingAddress: Boolean = false,
    val selectedAddressDetails: AddressDetails? = null,
    val error: String? = null
)

class AddressViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    init {
        fetchAddresses()
    }

    private fun fetchAddresses() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            authRepository.getUserProfileFlow(userId, UserRole.CUSTOMER)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { user ->
                    if (user != null) {
                        _uiState.update { it.copy(addresses = user.addresses, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            platformLocationManager.getCurrentLocation()
                .onSuccess { latLng ->
                    _uiState.update { it.copy(mapLocation = latLng) }
                    getAddressFromLatLng(latLng)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Could not get location: ${e.message}") }
                }
        }
    }

    fun getAddressFromLatLng(latLng: LatLng) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingAddress = true, mapLocation = latLng) }
            platformLocationManager.getAddressFromLatLng(latLng)
                .onSuccess { details ->
                    _uiState.update { it.copy(selectedAddressDetails = details, isFetchingAddress = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isFetchingAddress = false, error = e.message) }
                }
        }
    }

    fun saveAddress(
        label: String,
        fullName: String,
        mobileNumber: String,
        alternateNumber: String,
        flatHouseBuilding: String,
        areaLocality: String,
        city: String,
        state: String,
        pincode: String,
        details: AddressDetails,
        editedFullAddress: String
    ) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val newAddress = Address(
                id = "addr_${kotlin.random.Random.nextInt()}", // Simple ID generation
                label = label,
                fullName = fullName,
                mobileNumber = mobileNumber,
                alternateNumber = alternateNumber,
                flatHouseBuilding = flatHouseBuilding,
                areaLocality = areaLocality,
                city = city,
                state = state,
                pincode = pincode,
                fullAddress = editedFullAddress,
                latitude = details.lat,
                longitude = details.lng,
                isDefault = _uiState.value.addresses.isEmpty()
            )
            val updatedAddresses = _uiState.value.addresses + newAddress
            updateAddressesInRepo(userId, updatedAddresses)
        }
    }

    private suspend fun updateAddressesInRepo(userId: String, updatedAddresses: List<Address>) {
        authRepository.updateProfile(userId, mapOf("addresses" to updatedAddresses), UserRole.CUSTOMER)
            .onSuccess {
                _uiState.update { it.copy(showAddSheet = false, isSaving = false, selectedAddressDetails = null) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
    }

    fun deleteAddress(addressId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val updatedAddresses = _uiState.value.addresses.filter { it.id != addressId }
            updateAddressesInRepo(userId, updatedAddresses)
        }
    }

    fun setDefaultAddress(addressId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val updatedAddresses = _uiState.value.addresses.map {
                it.copy(isDefault = it.id == addressId)
            }
            updateAddressesInRepo(userId, updatedAddresses)
        }
    }

    fun toggleAddSheet(show: Boolean) {
        _uiState.update { it.copy(showAddSheet = show) }
    }
    
    fun clearSelectedAddress() {
        _uiState.update { it.copy(selectedAddressDetails = null) }
    }
}
