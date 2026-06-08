package com.maka.flymoney.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.maka.flymoney.domain.model.Round
import com.maka.flymoney.domain.model.RoundState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRtdbSource @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun observeCurrentRound(): Flow<Round> = callbackFlow {
        val ref = db.getReference("currentRound")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val round = snapshot.getValue(RoundDto::class.java)?.toDomain() ?: Round()
                trySend(round)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}

data class RoundDto(
    val roundId: String = "",
    val state: String = "WAITING",
    val multiplier: Double = 1.0,
    val countdownSec: Int = 0,
    val startTime: Long = 0L,
    val serverSeedHash: String? = null
) {
    fun toDomain() = Round(
        roundId = roundId,
        state = RoundState.valueOf(state),
        multiplier = multiplier,
        countdownSec = countdownSec,
        startTime = startTime,
        serverSeedHash = serverSeedHash
    )
}
