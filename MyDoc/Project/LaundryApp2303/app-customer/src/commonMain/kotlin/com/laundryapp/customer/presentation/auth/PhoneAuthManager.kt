package com.laundryapp.customer.presentation.auth

import dev.gitlive.firebase.auth.AuthCredential

interface PhoneAuthCallback {
    fun onCodeSent(verificationId: String)
    fun onVerificationCompleted(credential: AuthCredential)
    fun onError(error: String)
}

expect class PhoneAuthManager {
    fun sendOtp(phoneNumber: String, callback: PhoneAuthCallback)
}
