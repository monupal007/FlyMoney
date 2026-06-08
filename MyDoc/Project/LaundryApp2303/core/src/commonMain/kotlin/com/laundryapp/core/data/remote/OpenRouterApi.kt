package com.laundryapp.core.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

class OpenRouterApi(private val client: HttpClient) {
    suspend fun generateCompletion(apiKey: String, request: OpenRouterRequest): Result<OpenRouterResponse> = runCatching {
        client.post("https://openrouter.ai/api/v1/chat/completions") {
            header(HttpHeaders.Authorization, apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
