package com.laundryapp.core.util

interface ImageUploader {
    suspend fun uploadImage(imageData: Any): Result<String>
}
