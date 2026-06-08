package com.maka.flymoney.domain.repository

import com.maka.flymoney.domain.model.Round
import com.maka.flymoney.domain.model.Bet
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val currentRound: Flow<Round>
    val liveBets: Flow<List<Bet>>
    val roundHistory: Flow<List<Double>>
    
    suspend fun placeBet(amount: Double, autoCashoutAt: Double?): Result<Unit>
    suspend fun cashOut(): Result<Double> // returns winnings
    suspend fun cancelBet(): Result<Unit>
}
