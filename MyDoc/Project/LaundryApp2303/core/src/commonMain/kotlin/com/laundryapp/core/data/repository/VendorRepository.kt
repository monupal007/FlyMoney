package com.laundryapp.core.data.repository

import com.laundryapp.core.data.model.Vendor
import com.laundryapp.core.util.ImageUploader
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VendorRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val imageUploader: ImageUploader
) {
    private val vendorCollection = firestore.collection("vendors")

    suspend fun registerVendor(vendor: Vendor): Result<Unit> = runCatching {
        vendorCollection.document(vendor.vendorId).set(vendor)
    }

    suspend fun getVendor(vendorId: String): Result<Vendor?> = runCatching {
        val doc = vendorCollection.document(vendorId).get()
        doc.data<Vendor>()
    }

    fun getVendorFlow(vendorId: String): Flow<Vendor?> {
        return vendorCollection.document(vendorId).snapshots.map { it.data<Vendor>() }
    }

    fun getAllVendors(): Flow<List<Vendor>> {
        return vendorCollection.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<Vendor>() }
        }
    }
    
    suspend fun uploadDocument(vendorId: String, fileName: String, imageData: Any): Result<String> = runCatching {
        imageUploader.uploadImage(imageData).getOrThrow()
    }
}
