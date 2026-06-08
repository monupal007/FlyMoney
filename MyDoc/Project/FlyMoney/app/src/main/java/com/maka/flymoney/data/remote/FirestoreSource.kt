package com.maka.flymoney.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maka.flymoney.domain.model.Transaction
import com.maka.flymoney.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(User::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun observeTransactions(uid: String): Flow<List<Transaction>> = callbackFlow {
        val query = firestore.collection("transactions")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(transactions)
        }
        awaitClose { subscription.remove() }
    }

    fun observeLeaderboard(period: String): Flow<List<User>> = callbackFlow {
        val query = firestore.collection("leaderboard").document(period)
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val players = snapshot?.get("players") as? List<Map<String, Any>>
            val users = players?.map { 
                User(
                    uid = it["uid"] as String,
                    username = it["username"] as String,
                    walletBalance = (it["totalWinnings"] as? Number)?.toDouble() ?: 0.0
                )
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { subscription.remove() }
    }

    fun observeRoundHistory(): Flow<List<Double>> = callbackFlow {
        val query = firestore.collection("rounds")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val history = snapshot?.documents?.mapNotNull { it.getDouble("crashAt") } ?: emptyList()
            trySend(history)
        }
        awaitClose { subscription.remove() }
    }
}
