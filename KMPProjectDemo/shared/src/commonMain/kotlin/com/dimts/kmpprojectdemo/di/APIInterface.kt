package com.dimts.kmpprojectdemo.di

import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse

interface APIInterface {
    suspend fun fetch(): ApiResponse
    suspend fun fetchCompanyList(): ArrayList<CompanyListResponse>
    suspend fun fetchChatResponse(userMessage: String): String

    //    @POST("https://api.openai.com/v1/chat/completions")
//    suspend fun sendMessage(@Body request: GPTChatRequest): ApiResponse<GPTChatResponse>
}