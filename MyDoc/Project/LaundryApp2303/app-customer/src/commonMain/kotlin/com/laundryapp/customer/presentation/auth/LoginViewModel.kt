package com.laundryapp.customer.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.data.repository.AuthRepository
import dev.gitlive.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null,
    val isOtpSent: Boolean = false,
    val countryCode: String = "+91"
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val phoneAuthManager: PhoneAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onCountryCodeChange(code: String) = _uiState.update { it.copy(countryCode = code) }

    fun resetOtpSent() = _uiState.update { it.copy(isOtpSent = false, verificationId = null, error = null) }

    fun setError(message: String) = _uiState.update { it.copy(error = message, isLoading = false) }

    fun signInWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        // In KMP, Google Sign-In usually provides a credential directly or we use the token
        // This depends on the specific GMS KMP wrapper or platform implementation
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // authRepository.signInWithGoogle(idToken) // Need to add this back to AuthRepository if needed
            // For now, let's assume we use a credential from platform side
        }
    }

    fun sendOtp(phoneNumber: String) {
        val fullPhoneNumber = _uiState.value.countryCode + phoneNumber.trim()
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        phoneAuthManager.sendOtp(fullPhoneNumber, object : PhoneAuthCallback {
            override fun onCodeSent(verificationId: String) {
                _uiState.update { it.copy(isLoading = false, isOtpSent = true, verificationId = verificationId) }
            }

            override fun onVerificationCompleted(credential: AuthCredential) {
                signInWithCredential(credential, { /* Success callback */ })
            }

            override fun onError(error: String) {
                _uiState.update { it.copy(isLoading = false, error = error) }
            }
        })
    }

    fun signInWithCredential(credential: AuthCredential, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithCredential(credential)
                .onSuccess { user -> 
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user.role) 
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
