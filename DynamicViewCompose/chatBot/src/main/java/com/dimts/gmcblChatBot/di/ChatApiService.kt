package com.dimts.gmcblChatBot.di

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {

    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    @FormUrlEncoded
    @POST("api/route")
    suspend fun getBotReply(
        @Field("reqStr") reqStr: String
    ): Response<ResponseBody>

}
