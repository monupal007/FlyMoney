package com.maka.flymoney.domain.usecase

import com.maka.flymoney.domain.model.User
import com.maka.flymoney.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(uid: String): Flow<User?> = repository.getUserProfile(uid)
}
