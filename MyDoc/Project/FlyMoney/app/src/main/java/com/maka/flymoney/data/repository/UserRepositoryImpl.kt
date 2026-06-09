package com.maka.flymoney.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maka.flymoney.data.remote.FirestoreSource
import com.maka.flymoney.domain.model.Transaction as DomainTransaction
import com.maka.flymoney.domain.model.TransactionType
import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<User?> {
        return firestoreSource.observeUser(uid)
    }

    override fun getTransactionHistory(uid: String): Flow<List<DomainTransaction>> {
        return firestoreSource.observeTransactions(uid)
    }

    override fun getLeaderboard(period: String): Flow<List<User>> {
        return firestoreSource.observeLeaderboard(period)
    }

    override suspend fun updateProfile(uid: String, username: String, avatarUrl: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            if (username.isNotBlank()) updates["username"] = username
            if (avatarUrl.isNotBlank()) updates["avatarUrl"] = avatarUrl
            
            if (updates.isNotEmpty()) {
                // Use set with merge to create document if it doesn't exist
                firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deposit(uid: String, amount: Double): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(uid)
            val txRef = firestore.collection("transactions").document()
            
            val transactionData = DomainTransaction(
                id = txRef.id,
                uid = uid,
                amount = amount,
                type = TransactionType.DEPOSIT,
                timestamp = System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) {
                    // Create user if missing
                    val newUser = User(
                        uid = uid,
                        walletBalance = amount,
                        createdAt = System.currentTimeMillis()
                    )
                    transaction.set(userRef, newUser)
                } else {
                    // Update balance if exists
                    transaction.update(userRef, "walletBalance", FieldValue.increment(amount))
                }
                transaction.set(txRef, transactionData)
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun withdraw(uid: String, amount: Double): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(uid)
            val txRef = firestore.collection("transactions").document()
            
            val transactionData = DomainTransaction(
                id = txRef.id,
                uid = uid,
                amount = amount,
                type = TransactionType.WITHDRAW,
                timestamp = System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) {
                    throw Exception("User profile not found")
                }
                val currentBalance = snapshot.getDouble("walletBalance") ?: 0.0
                if (currentBalance < amount) {
                    throw Exception("Insufficient balance")
                }
                transaction.update(userRef, "walletBalance", FieldValue.increment(-amount))
                transaction.set(txRef, transactionData)
                null
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
