package com.laundryapp.core.data.remote

import com.laundryapp.shared.models.NotificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApi {
    @POST("send-notification")
    suspend fun sendNotification(
        @Body request: NotificationRequest
    ): Response<Map<String, String>>
}
