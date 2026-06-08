package com.maka.flymoney.data.repository

import com.maka.flymoney.data.remote.GameWebSocketClient
import com.maka.flymoney.domain.model.ChatMessage
import com.maka.flymoney.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val wsClient: GameWebSocketClient
) : ChatRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            wsClient.events.collect { event ->
                if (event is GameWebSocketClient.GameEvent.ChatReceived) {
                    val newMessage = ChatMessage(
                        uid = event.uid,
                        username = event.username,
                        message = event.message,
                        timestamp = event.timestamp
                    )
                    _messages.value = (_messages.value + newMessage).takeLast(100)
                }
            }
        }
    }

    override fun getMessages(): Flow<List<ChatMessage>> {
        return _messages.asStateFlow()
    }

    override suspend fun sendMessage(message: String): Result<Unit> {
        return try {
            wsClient.sendChat(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
