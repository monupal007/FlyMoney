package com.laundryapp.customer.presentation.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.laundryapp.core.data.model.UserRole
import com.laundryapp.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null,
    val isOtpSent: Boolean = false,
    val countryCode: String = "+91"
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onCountryCodeChange(code: String) = _uiState.update { it.copy(countryCode = code) }

    fun resetOtpSent() = _uiState.update { it.copy(isOtpSent = false, verificationId = null, error = null) }

    fun setError(message: String) = _uiState.update { it.copy(error = message, isLoading = false) }

    fun signInWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user -> 
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user.role) 
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        val fullPhoneNumber = _uiState.value.countryCode + phoneNumber.trim()
        _uiState.update { it.copy(isLoading = true, error = null) }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    signInWithPhone(credential, { /* Handled in UI via navigation */ })
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _uiState.update { it.copy(isLoading = false, isOtpSent = true, verificationId = verificationId) }
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhone(credential: com.google.firebase.auth.PhoneAuthCredential, onSuccess: (UserRole) -> Unit) {
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
