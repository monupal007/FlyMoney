package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Vendor(
    val vendorId: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.VENDOR,
    val shopName: String = "",
    val services: List<String> = emptyList(),
    val serviceItems: Map<String, List<String>> = emptyMap(),
    val itemPrices: Map<String, Double> = emptyMap(),
    val experience: Int = 0,
    val address: String = "",
    val location: LatLngData = LatLngData(),
    val serviceRadius: Int = 5,
    val bankDetails: BankDetails = BankDetails(),
    val documents: VendorDocuments = VendorDocuments(),
    var isApproved: Boolean = false,
    var isRejected: Boolean = false,
    var isOnDuty: Boolean = true,
    val commission: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val createdAt: Long = 0L,
    val rating: Double = 0.0,
    val totalRatings: Long = 0L
)

@Serializable
data class LatLngData(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

@Serializable
data class BankDetails(
    val accountNumber: String = "",
    val ifsc: String = "",
    val upi: String = ""
)

@Serializable
data class VendorDocuments(
    val aadhaarUrl: String = "",
    val panUrl: String = "",
    val shopLicenseUrl: String = ""
)
