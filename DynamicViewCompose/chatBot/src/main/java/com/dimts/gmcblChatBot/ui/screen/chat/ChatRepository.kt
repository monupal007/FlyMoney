package com.dimts.gmcblChatBot.ui.screen.chat

interface ChatRepository {
    suspend fun getBotReply(message: String, deviceId: String): String
}