package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LaundryService(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val priceMultiplier: Double = 1.0,
    val iconUrl: String = "",
    val estimatedDays: Int = 2,
    var isAvailable: Boolean = true,
    var category: String = "" 
)

@Serializable
data class LaundryItem(
    val id: String = "",
    val name: String = "",
    val basePrice: Double = 0.0,
    val category: String = "",
    val iconUrl: String = ""
)
