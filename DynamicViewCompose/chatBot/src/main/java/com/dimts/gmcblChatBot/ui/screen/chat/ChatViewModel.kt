package com.dimts.gmcblChatBot.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val deviceId: String
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state
    private var currentJob: Job? = null

    fun sendMessage(text: String, messageId: String? = null) {
        if (_state.value.isTyping) return // Prevent duplicate sends

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            type = MessageType.USER
        )

        _state.update { currentState ->
            val updatedMessages = currentState.messages.map { msg ->
                msg.copy(isActionable = false)
            }
            currentState.copy(
                messages = updatedMessages + userMessage,
                isTyping = true
            )
        }

        currentJob = viewModelScope.launch {
            try {
                val reply = sendMessageUseCase(text, deviceId)

                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = reply.toString(),
                    type = MessageType.BOT
                )

                _state.update {
                    it.copy(
                        messages = it.messages + botMessage,
                        isTyping = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isTyping = false) }
            } finally {
                currentJob = null
            }
        }
    }

    fun stopMessage() {
        currentJob?.cancel()
        currentJob = null
        _state.update { it.copy(isTyping = false) }
    }
}