package com.maka.flymoney.domain.repository

import com.maka.flymoney.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: String): Result<Unit>
}
