package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.repository.GameRepository
import javax.inject.Inject

class PlaceBetUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(amount: Double, autoCashoutAt: Double?): Result<Unit> {
        return repository.placeBet(amount, autoCashoutAt)
    }
}
