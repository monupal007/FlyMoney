package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLeaderboardUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(period: String): Flow<List<User>> = repository.getLeaderboard(period)
}
