package com.laundryapp.core.util

data class LatLng(val latitude: Double, val longitude: Double)

data class AddressDetails(
    val fullAddress: String,
    val houseNumber: String,
    val street: String,
    val locality: String,
    val city: String,
    val state: String,
    val pincode: String,
    val lat: Double,
    val lng: Double
)

interface LocationManager {
    suspend fun getCurrentLocation(): Result<LatLng>
    suspend fun getAddressFromLatLng(latLng: LatLng): Result<AddressDetails>
}

expect val platformLocationManager: LocationManager
