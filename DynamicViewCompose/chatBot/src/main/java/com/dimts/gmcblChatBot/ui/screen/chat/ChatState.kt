package com.dimts.gmcblChatBot.ui.screen.chat

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)