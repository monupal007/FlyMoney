package com.laundryapp.customer.presentation.auth

import android.app.Activity
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.AuthCredential
import java.util.concurrent.TimeUnit

actual class PhoneAuthManager(private val activity: Activity) {
    private val auth = FirebaseAuth.getInstance()

    actual fun sendOtp(phoneNumber: String, callback: PhoneAuthCallback) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    // Convert Android credential to GitLive credential if possible, or handle separately
                    // callback.onVerificationCompleted(...)
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    callback.onError(e.message ?: "Verification failed")
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    callback.onCodeSent(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
