package com.laundryapp.core.presentation.driver.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.*
import com.laundryapp.core.data.repository.AuthRepository
import com.laundryapp.core.data.repository.DriverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DriverRegistrationUiState(
    val step: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    
    // Step 1: Personal Info
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    
    // Step 2: Vehicle & Location
    val vehicleType: String = "Bike",
    val vehicleNumber: String = "",
    val city: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    
    // Step 3: Bank Details
    val accountNumber: String = "",
    val ifsc: String = "",
    val upi: String = "",
    
    // Step 4: Documents (Using Any for platform-specific URI/File)
    val aadhaarData: Any? = null,
    val licenseData: Any? = null,
    val rcData: Any? = null
)

class DriverRegistrationViewModel(
    private val authRepository: AuthRepository,
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverRegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    
    fun onVehicleTypeChange(value: String) = _uiState.update { it.copy(vehicleType = value) }
    fun onVehicleNumberChange(value: String) = _uiState.update { 
        it.copy(vehicleNumber = value.replace(" ", "").uppercase()) 
    }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onLocationChange(lat: Double, lng: Double) = _uiState.update { it.copy(lat = lat, lng = lng) }
    
    fun onAccountChange(value: String) = _uiState.update { it.copy(accountNumber = value) }
    fun onIfscChange(value: String) = _uiState.update { it.copy(ifsc = value) }
    fun onUpiChange(value: String) = _uiState.update { it.copy(upi = value) }
    
    fun onAadhaarSelected(data: Any?) = _uiState.update { it.copy(aadhaarData = data) }
    fun onLicenseSelected(data: Any?) = _uiState.update { it.copy(licenseData = data) }
    fun onRcSelected(data: Any?) = _uiState.update { it.copy(rcData = data) }

    fun nextStep() {
        val state = _uiState.value
        when(state.step) {
            1 -> {
                if (state.name.isBlank() || state.phone.isBlank() || state.password.isBlank() || state.email.isBlank()) {
                    _uiState.update { it.copy(error = "Please fill all required fields") }
                    return
                }
                if (state.phone.length < 10) {
                    _uiState.update { it.copy(error = "Please enter a valid phone number") }
                    return
                }
            }
            2 -> {
                if (state.accountNumber.isBlank() || state.ifsc.isBlank()) {
                    _uiState.update { it.copy(error = "Please fill bank details") }
                    return
                }
            }
        }
        _uiState.update { it.copy(step = it.step + 1, error = null) }
    }
    
    fun prevStep() = _uiState.update { it.copy(step = it.step - 1, error = null) }

    fun registerDriver() {
        val state = _uiState.value
        if (state.aadhaarData == null || state.licenseData == null || state.rcData == null) {
            _uiState.update { it.copy(error = "Please upload all required documents") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val signUpResult = authRepository.signUpWithEmail(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    phone = state.phone,
                    role = UserRole.DELIVERY_PARTNER
                )
                val user = signUpResult.getOrThrow()
                val userId = user.id

                val aadhaarUrl = driverRepository.uploadDocument(userId, "aadhaar", state.aadhaarData!!).getOrThrow()
                val licenseUrl = driverRepository.uploadDocument(userId, "license", state.licenseData!!).getOrThrow()
                val rcUrl = driverRepository.uploadDocument(userId, "rc", state.rcData!!).getOrThrow()

                val driver = Driver(
                    driverId = userId,
                    name = state.name,
                    phone = state.phone,
                    email = state.email,
                    vehicleType = state.vehicleType,
                    vehicleNumber = state.vehicleNumber,
                    city = state.city,
                    location = LatLngData(state.lat, state.lng),
                    bankDetails = BankDetails(state.accountNumber, state.ifsc, state.upi),
                    documents = DriverDocuments(aadhaarUrl, licenseUrl, rcUrl),
                    isApproved = false
                )

                driverRepository.registerDriver(driver).getOrThrow()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
