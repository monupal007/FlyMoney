package com.dimts.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimts.myapplication.core.utils.Resource
import com.dimts.myapplication.domain.model.RegistrationResponse
import com.dimts.myapplication.domain.usecase.VerifyOtpUseCase
import com.dimts.myapplication.domain.usecase.ResendOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val resendOtpUseCase: ResendOtpUseCase
) : ViewModel() {

    private val _otpVerificationState = MutableStateFlow<Resource<RegistrationResponse>?>(null)
    val otpVerificationState: StateFlow<Resource<RegistrationResponse>?> =
        _otpVerificationState.asStateFlow()

    private val _resendOtpState = MutableStateFlow<Resource<RegistrationResponse>?>(null)
    val resendOtpState: StateFlow<Resource<RegistrationResponse>?> =
        _resendOtpState.asStateFlow()

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun updateOtp(otp: String) {
        // Allow only digits and limit to 6 digits
        val filteredOtp = otp.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(otp = filteredOtp, otpError = null)
    }

    fun verifyOtp(mobileNumber: String, otp: String) {
        val currentState = _uiState.value

        // Validate OTP
        if (otp.isBlank()) {
            _uiState.value = currentState.copy(
                otpError = "Please enter the OTP"
            )
            return
        }

        if (otp.length != 6) {
            _uiState.value = currentState.copy(
                otpError = "Please enter a valid 6-digit OTP"
            )
            return
        }

        viewModelScope.launch {
            verifyOtpUseCase(mobileNumber, otp).collect { resource ->
                _otpVerificationState.value = resource
            }
        }
    }

    fun resendOtp(mobileNumber: String) {
        if (mobileNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(
                otpError = "Mobile number is required"
            )
            return
        }

        viewModelScope.launch {
            resendOtpUseCase(mobileNumber).collect { resource ->
                _resendOtpState.value = resource
            }
        }
    }

    fun clearOtpVerificationState() {
        _otpVerificationState.value = null
    }

    fun clearResendOtpState() {
        _resendOtpState.value = null
    }
}

data class OtpUiState(
    val otp: String = "",
    val otpError: String? = null
)