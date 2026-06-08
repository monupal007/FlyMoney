package com.maka.flymoney.domain.model

data class ChatMessage(
    val uid: String = "",
    val username: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)
