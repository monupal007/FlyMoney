package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class OfferType {
    FLAT, PERCENTAGE
}

@Serializable
data class Offer(
    val id: String = "",
    val vendorId: String = "",
    val title: String = "",
    val description: String = "",
    val code: String = "",
    val type: OfferType = OfferType.PERCENTAGE,
    val value: Double = 0.0,
    val maxDiscount: Double? = null,
    val minOrderAmount: Double = 0.0,
    val applicableServices: List<String> = emptyList(),
    val applicableVendors: List<String> = emptyList(),
    val applicableItems: List<String> = emptyList(),
    val usageLimitPerUser: Int = 1,
    val totalUsageLimit: Int = 1000,
    val startDate: Long? = null, // Using Long for KMP compatibility (milliseconds)
    val endDate: Long? = null,
    val active: Boolean = true,
    val approved: Boolean = false,
    val rejected: Boolean = false,
    val createdAt: Long? = null,
    val imageUrl: String = ""
) {
    val isActive: Boolean get() = active
    val isApproved: Boolean get() = approved
    val isRejected: Boolean get() = rejected
}
