package com.laundryapp.core.util

import kotlin.math.*

object LocationUtils {
    /**
     * Calculates distance between two points in Kilometers using Haversine formula
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the earth in km
        val dLat = deg2rad(lat2 - lat1)
        val dLon = deg2rad(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (PI / 180)
    }

    fun formatDistance(km: Double): String {
        return if (km < 1.0) {
            "${(km * 1000).toInt()} m"
        } else {
            // Simple rounding for KMP commonMain
            val rounded = (km * 10).toInt() / 10.0
            "$rounded km"
        }
    }
}
