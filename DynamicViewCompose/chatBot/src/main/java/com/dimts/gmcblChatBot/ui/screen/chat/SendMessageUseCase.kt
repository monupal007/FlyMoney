package com.dimts.gmcblChatBot.ui.screen.chat

class SendMessageUseCase(
    private val repository: ChatRepository,

) {
    suspend operator fun invoke(message: String, deviceId: String): Any {

        return  repository.getBotReply(message, deviceId)

//        val welcomeGreetings = listOf("hello", "hi", "hey", "good morning")
//
//        return when {
//            welcomeGreetings.any { message.equals(it, ignoreCase = true) } -> {
//                """
//        {
//        "views":[
//        {
//        "type":"column_text",
//        "options":["Welcome to GMCBL Chat Bot 👋 ", "How can I help you? "]
//        }
//        ]
//        }
//        """.trimIndent()
//            }
//
//            else -> {
//                repository.getBotReply(message, deviceId)
//            }
//        }
    }
}