package com.laundryapp.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.*

class AndroidLocationManager(private val context: Context) : LocationManager {
    
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<LatLng> = runCatching {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await() ?: throw Exception("Location is null")
        LatLng(location.latitude, location.longitude)
    }

    override suspend fun getAddressFromLatLng(latLng: LatLng): Result<AddressDetails> = runCatching {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (addresses.isNullOrEmpty()) throw Exception("No address found")
        
        val addr = addresses[0]
        AddressDetails(
            fullAddress = addr.getAddressLine(0) ?: "",
            houseNumber = addr.featureName ?: "",
            street = addr.thoroughfare ?: "",
            locality = addr.subLocality ?: addr.locality ?: "",
            city = addr.locality ?: "",
            state = addr.adminArea ?: "",
            pincode = addr.postalCode ?: "",
            lat = latLng.latitude,
            lng = latLng.longitude
        )
    }
}

actual val platformLocationManager: LocationManager
    get() = throw Exception("Use Koin to inject the platform implementation")
