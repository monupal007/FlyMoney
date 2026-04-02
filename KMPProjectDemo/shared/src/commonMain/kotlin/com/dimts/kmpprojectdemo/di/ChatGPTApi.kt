package com.dimts.kmpprojectdemo.di

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ChatGPTApi(private val apiKey: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getReply(userMessage: String): String {
        val response: ChatGPTResponse = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(
                ChatGPTRequest(
                    model = "gpt-4o-mini",
                    messages = listOf(ChatMessage("user", userMessage))
                )
            )
        }.body()

        return response.choices.firstOrNull()?.message?.content ?: "No response"
    }
}

@Serializable
data class ChatGPTRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatGPTResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ChatMessage
)