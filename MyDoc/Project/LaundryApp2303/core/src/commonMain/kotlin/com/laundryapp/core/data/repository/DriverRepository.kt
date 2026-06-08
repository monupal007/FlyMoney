package com.laundryapp.core.data.repository

import com.laundryapp.core.data.model.Driver
import com.laundryapp.core.data.model.LatLngData
import com.laundryapp.core.util.ImageUploader
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DriverRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val imageUploader: ImageUploader
) {
    private val driverCollection = firestore.collection("drivers")

    suspend fun registerDriver(driver: Driver): Result<Unit> = runCatching {
        driverCollection.document(driver.driverId).set(driver)
    }

    suspend fun getDriver(driverId: String): Result<Driver?> = runCatching {
        val doc = driverCollection.document(driverId).get()
        doc.data<Driver>()
    }

    fun getDriverFlow(driverId: String): Flow<Driver?> {
        return driverCollection.document(driverId).snapshots.map { it.data<Driver>() }
    }

    fun getAllDrivers(): Flow<List<Driver>> {
        return driverCollection.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<Driver>() }
        }
    }

    suspend fun getApprovedDrivers(): List<Driver> {
        return try {
            val snapshot = driverCollection.where { "isApproved" equalTo true }.get()
            snapshot.documents.map { it.data<Driver>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateLocation(driverId: String, lat: Double, lng: Double): Result<Unit> = runCatching {
        driverCollection.document(driverId).update("location" to LatLngData(lat, lng))
    }

    suspend fun updateDriverStatus(driverId: String, isOnline: Boolean): Result<Unit> = runCatching {
        driverCollection.document(driverId).update("isOnline" to isOnline)
    }
    
    suspend fun uploadDocument(driverId: String, fileName: String, imageData: Any): Result<String> = runCatching {
        imageUploader.uploadImage(imageData).getOrThrow()
    }
}
