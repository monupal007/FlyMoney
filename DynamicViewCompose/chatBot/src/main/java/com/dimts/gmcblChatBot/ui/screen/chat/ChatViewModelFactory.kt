package com.dimts.gmcblChatBot.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(
    private val sendMessageUseCase: SendMessageUseCase,
    private val deviceId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(sendMessageUseCase, deviceId) as T
    }
}