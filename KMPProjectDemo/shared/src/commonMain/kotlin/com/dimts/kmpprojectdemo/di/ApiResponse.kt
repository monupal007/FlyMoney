package com.dimts.kmpprojectdemo.di

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse (
    val userId: Int = 0,
    val id: Int = 0,
    val title: String = "",
    val body: String = ""
)