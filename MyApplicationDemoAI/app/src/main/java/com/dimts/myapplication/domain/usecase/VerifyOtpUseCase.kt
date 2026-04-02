package com.dimts.myapplication.domain.usecase

import com.dimts.myapplication.core.utils.Resource
import com.dimts.myapplication.domain.model.RegistrationResponse
import com.dimts.myapplication.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(mobileNumber: String, otp: String): Flow<Resource<RegistrationResponse>> {
        // Validate mobile number
        if (mobileNumber.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Error("Mobile number is required"))
        }

        if (mobileNumber.length != 10) {
            return kotlinx.coroutines.flow.flowOf(Resource.Error("Please enter a valid 10-digit mobile number"))
        }

        // Validate OTP
        if (otp.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Resource.Error("OTP is required"))
        }

        if (otp.length != 6) {
            return kotlinx.coroutines.flow.flowOf(Resource.Error("Please enter a valid 6-digit OTP"))
        }

        return userRepository.verifyOtp(mobileNumber, otp)
    }
}