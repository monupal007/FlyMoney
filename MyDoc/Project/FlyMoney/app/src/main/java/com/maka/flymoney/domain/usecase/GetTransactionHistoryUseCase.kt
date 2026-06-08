package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.model.Transaction
import com.maka.flymoney.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionHistoryUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(uid: String): Flow<List<Transaction>> = repository.getTransactionHistory(uid)
}
