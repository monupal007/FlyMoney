package com.laundryapp.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val targetToken: String,
    val title: String,
    val body: String,
    val data: Map<String, String>? = null
)
