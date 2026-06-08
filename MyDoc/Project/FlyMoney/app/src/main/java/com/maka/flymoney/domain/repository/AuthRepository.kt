package com.maka.flymoney.domain.repository

import com.maka.flymoney.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(username: String, email: String, password: String): Result<Unit>
    suspend fun loginWithPhone(verificationId: String, code: String): Result<Unit>
    suspend fun logout()
    suspend fun getIdToken(): String?
}
