package com.dimts.gmcblChatBot.ui.screen.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val type: MessageType,
    val isActionable: Boolean = true,
    var isTyped: Boolean = false,
    val time: Long = System.currentTimeMillis()
)

enum class MessageType {
    USER, BOT
}