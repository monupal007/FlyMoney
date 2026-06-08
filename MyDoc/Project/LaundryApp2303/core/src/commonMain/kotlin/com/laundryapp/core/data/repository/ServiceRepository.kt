package com.laundryapp.core.data.repository

import com.laundryapp.core.data.model.LaundryItem
import com.laundryapp.core.data.model.LaundryService
import com.laundryapp.core.data.model.Offer
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val serviceCollection = firestore.collection("services")
    private val offerCollection = firestore.collection("offers")

    fun getServices(): Flow<List<LaundryService>> {
        return serviceCollection.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<LaundryService>() }
        }
    }

    suspend fun addService(service: LaundryService): Result<Unit> = runCatching {
        serviceCollection.document(service.id).set(service)
    }

    suspend fun updateService(service: LaundryService): Result<Unit> = runCatching {
        serviceCollection.document(service.id).set(service)
    }

    suspend fun deleteService(serviceId: String): Result<Unit> = runCatching {
        serviceCollection.document(serviceId).delete()
    }

    fun getLaundryItems(serviceId: String): Flow<List<LaundryItem>> {
        return serviceCollection.document(serviceId).collection("items").snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<LaundryItem>() }
        }
    }

    suspend fun addLaundryItem(serviceId: String, item: LaundryItem): Result<Unit> = runCatching {
        serviceCollection.document(serviceId).collection("items").document(item.id).set(item)
    }

    suspend fun updateLaundryItem(serviceId: String, item: LaundryItem): Result<Unit> = runCatching {
        serviceCollection.document(serviceId).collection("items").document(item.id).set(item)
    }

    suspend fun deleteLaundryItem(serviceId: String, itemId: String): Result<Unit> = runCatching {
        serviceCollection.document(serviceId).collection("items").document(itemId).delete()
    }

    fun getOffers(): Flow<List<Offer>> {
        return offerCollection.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<Offer>() }
        }
    }

    suspend fun addOffer(offer: Offer): Result<Unit> = runCatching {
        offerCollection.document(offer.id).set(offer)
    }

    suspend fun approveOffer(offerId: String): Result<Unit> = runCatching {
        offerCollection.document(offerId).update("approved" to true, "rejected" to false)
    }

    suspend fun rejectOffer(offerId: String): Result<Unit> = runCatching {
        offerCollection.document(offerId).update("approved" to false, "rejected" to true)
    }

    suspend fun deleteOffer(offerId: String): Result<Unit> = runCatching {
        offerCollection.document(offerId).delete()
    }
}
