package com.maka.flymoney.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.maka.flymoney.data.remote.FirebaseRtdbSource
import com.maka.flymoney.data.remote.FirestoreSource
import com.maka.flymoney.data.remote.GameServerClient
import com.maka.flymoney.data.remote.GameWebSocketClient
import com.maka.flymoney.domain.model.Bet
import com.maka.flymoney.domain.model.Round
import com.maka.flymoney.domain.repository.GameRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val rtdbSource: FirebaseRtdbSource,
    private val firestoreSource: FirestoreSource,
    private val serverClient: GameServerClient,
    private val wsClient: GameWebSocketClient,
    private val db: FirebaseDatabase
) : GameRepository {

    override val currentRound: Flow<Round> = rtdbSource.observeCurrentRound()

    override val liveBets: Flow<List<Bet>> = callbackFlow {
        // Implementation would observe /rounds/{roundId}/bets in RTDB if needed
        // For now, GameViewModel handles this via WebSocket for lower latency
        trySend(emptyList())
        awaitClose { }
    }

    override val roundHistory: Flow<List<Double>> = firestoreSource.observeRoundHistory()

    override suspend fun placeBet(amount: Double, autoCashoutAt: Double?): Result<Unit> {
        return try {
            val response = serverClient.placeBet(amount, autoCashoutAt)
            if (response.isSuccess) Result.success(Unit)
            else Result.failure(response.exceptionOrNull() ?: Exception("Bet failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cashOut(): Result<Double> {
        return try {
            val response = serverClient.cashOut()
            if (response.isSuccess) Result.success(response.getOrThrow().winnings)
            else Result.failure(response.exceptionOrNull() ?: Exception("Cashout failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBet(): Result<Unit> {
        // Implementation would call server to cancel a pending bet
        return Result.success(Unit)
    }
}
