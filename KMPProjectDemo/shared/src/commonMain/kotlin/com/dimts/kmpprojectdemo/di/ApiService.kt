package com.dimts.kmpprojectdemo.di

import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.koin.core.annotation.Single


@Single
class ApiService(private val client: HttpClient) : APIInterface {

    override suspend fun fetch(): ApiResponse {
        val url = "https://jsonplaceholder.typicode.com/posts/1"
        return client.get(url).body()
    }

    override suspend fun fetchCompanyList(): ArrayList<CompanyListResponse> {
        val raw = "https://fake-json-api.mock.beeceptor.com/companies"
        return client.get(raw).body()
    }

    override suspend fun fetchChatResponse(userMessage: String): String {
        val response: ChatGPTResponse = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer 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")
            contentType(ContentType.Application.Json)
            setBody(
                ChatGPTRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(ChatMessage("user", userMessage))
                )
            )
        }.body()

        return response.choices.firstOrNull()?.message?.content ?: "No response"

    }
}