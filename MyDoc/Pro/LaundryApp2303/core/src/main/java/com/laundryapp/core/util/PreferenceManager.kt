package com.laundryapp.core.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("laundry_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HAS_SEEN_BOOKING_TOUR = "has_seen_booking_tour"
    }

    fun hasSeenBookingTour(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_BOOKING_TOUR, false)
    }

    fun setHasSeenBookingTour(hasSeen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_BOOKING_TOUR, hasSeen).apply()
    }
}
