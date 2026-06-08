package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    val driverId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.DELIVERY_PARTNER,
    val vehicleType: String = "",
    val vehicleNumber: String = "",
    val city: String = "",
    val location: LatLngData = LatLngData(),
    val bankDetails: BankDetails = BankDetails(),
    val documents: DriverDocuments = DriverDocuments(),
    var isApproved: Boolean = false,
    var isRejected: Boolean = false,
    var isOnline: Boolean = false,
    var isBusy: Boolean = false,
    val totalEarnings: Double = 0.0,
    val createdAt: Long = 0L,
    val rating: Double = 0.0,
    val totalRatings: Long = 0L
)

@Serializable
data class DriverDocuments(
    val aadhaarUrl: String = "",
    val licenseUrl: String = "",
    val rcUrl: String = ""
)
