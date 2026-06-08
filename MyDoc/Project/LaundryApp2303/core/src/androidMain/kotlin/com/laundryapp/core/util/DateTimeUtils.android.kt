package com.laundryapp.core.util

import java.text.SimpleDateFormat
import java.util.*

actual object DateTimeUtils {
    actual fun formatDateTime(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown date"
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    actual fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown date"
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
