package com.dimts.gmcblChatBot.di

import okhttp3.ResponseBody
import retrofit2.Response

class ApiService : ChatApiService {
    // Initializing ApiInterface implementation using Retrofit
    private val apiCall = RetrofitClass.retrofit.create(ChatApiService::class.java)

    override suspend fun getBotReply(reqStr: String): Response<ResponseBody> {
        return apiCall.getBotReply(reqStr)
    }
}
