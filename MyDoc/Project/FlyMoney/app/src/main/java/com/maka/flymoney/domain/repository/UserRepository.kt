package com.maka.flymoney.domain.repository

import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(uid: String): Flow<User?>
    fun getTransactionHistory(uid: String): Flow<List<Transaction>>
    fun getLeaderboard(period: String): Flow<List<User>>
    suspend fun updateProfile(uid: String, username: String, avatarUrl: String): Result<Unit>
    suspend fun deposit(uid: String, amount: Double): Result<Unit>
    suspend fun withdraw(uid: String, amount: Double): Result<Unit>
}
