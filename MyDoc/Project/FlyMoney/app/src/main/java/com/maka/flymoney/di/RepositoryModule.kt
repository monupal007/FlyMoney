package com.maka.flymoney.di

import com.maka.flymoney.data.repository.AuthRepositoryImpl
import com.maka.flymoney.data.repository.ChatRepositoryImpl
import com.maka.flymoney.data.repository.GameRepositoryImpl
import com.maka.flymoney.data.repository.UserRepositoryImpl
import com.maka.flymoney.domain.repository.AuthRepository
import com.maka.flymoney.domain.repository.ChatRepository
import com.maka.flymoney.domain.repository.GameRepository
import com.maka.flymoney.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
