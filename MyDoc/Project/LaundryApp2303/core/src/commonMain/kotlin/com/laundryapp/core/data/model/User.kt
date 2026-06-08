package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { 
    CUSTOMER, 
    VENDOR, 
    DELIVERY_PARTNER, 
    SUPER_ADMIN 
}

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val addresses: List<Address> = emptyList(),
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = 0L,
    
    // Vendor specific fields
    val businessName: String = "",
    var isApproved: Boolean = false,
    var isRejected: Boolean = false,
    val commissionRate: Double = 0.0,
    val rating: Double = 0.0
)

@Serializable
data class Address(
    val id: String = "",
    val label: String = "",        // "Home", "Work", etc.
    val fullName: String = "",
    val mobileNumber: String = "",
    val alternateNumber: String = "",
    val flatHouseBuilding: String = "",
    val areaLocality: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val fullAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var isDefault: Boolean = false
) {
    fun getFormattedAddress(): String {
        return buildString {
            if (fullName.isNotEmpty()) append("${fullName}\n")
            if (flatHouseBuilding.isNotEmpty()) append("${flatHouseBuilding}, ")
            if (areaLocality.isNotEmpty()) append("${areaLocality}\n")
            if (city.isNotEmpty()) append("${city}, ")
            if (state.isNotEmpty()) append("${state} - ")
            if (pincode.isNotEmpty()) append(pincode)
            if (mobileNumber.isNotEmpty()) append("\nPhone: ${mobileNumber}")
        }
    }
}
