package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.repository.GameRepository
import javax.inject.Inject

class CashOutUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(): Result<Double> {
        return repository.cashOut()
    }
}
