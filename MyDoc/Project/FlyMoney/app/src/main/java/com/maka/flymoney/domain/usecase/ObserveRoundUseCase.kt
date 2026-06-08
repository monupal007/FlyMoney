package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.model.Round
import com.maka.flymoney.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRoundUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(): Flow<Round> = repository.currentRound
}
