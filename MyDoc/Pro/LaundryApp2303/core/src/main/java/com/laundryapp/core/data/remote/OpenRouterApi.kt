package com.laundryapp.core.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") siteUrl: String = "https://laundryapp.com",
        @Header("X-Title") siteName: String = "Laundry App",
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}
