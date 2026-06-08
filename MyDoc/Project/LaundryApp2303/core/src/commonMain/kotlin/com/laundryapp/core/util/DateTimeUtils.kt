package com.laundryapp.core.util

expect object DateTimeUtils {
    fun formatDateTime(timestamp: Long): String
    fun formatDate(timestamp: Long): String
}
