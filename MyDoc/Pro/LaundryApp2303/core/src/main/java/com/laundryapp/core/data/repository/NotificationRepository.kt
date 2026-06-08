package com.laundryapp.core.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.laundryapp.core.data.remote.BackendApi
import com.laundryapp.shared.models.NotificationRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val backendApi: BackendApi,
    private val firestore: FirebaseFirestore
) {
    suspend fun notifyVendorNewOrder(vendorId: String, orderId: String) {
        try {
            val vendorDoc = firestore.collection("vendors").document(vendorId).get().await()
            val fcmToken = vendorDoc.getString("fcmToken")
            
            if (!fcmToken.isNullOrEmpty()) {
                val request = NotificationRequest(
                    targetToken = fcmToken,
                    title = "New Order Received!",
                    body = "You have a new order #${orderId.takeLast(8).uppercase()}",
                    data = mapOf(
                        "orderId" to orderId,
                        "type" to "NEW_ORDER"
                    )
                )
                backendApi.sendNotification(request)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error notifying vendor: ${e.message}")
        }
    }

    suspend fun notifyCustomerStatusUpdate(userId: String, orderId: String, status: String) {
        try {
            val customerDoc = firestore.collection("customers").document(userId).get().await()
            val fcmToken = customerDoc.getString("fcmToken")
            
            if (!fcmToken.isNullOrEmpty()) {
                val statusMsg = when (status) {
                    "CONFIRMED" -> "is Confirmed"
                    "PICKED_UP" -> "has been Picked Up"
                    "PROCESSING" -> "is being Processed"
                    "READY_FOR_DELIVERY" -> "is Ready for Delivery"
                    "OUT_FOR_DELIVERY" -> "is Out for Delivery"
                    "DELIVERED" -> "has been Delivered"
                    else -> "status is now $status"
                }

                val request = NotificationRequest(
                    targetToken = fcmToken,
                    title = "Order Update",
                    body = "Your order #${orderId.takeLast(8).uppercase()} $statusMsg",
                    data = mapOf(
                        "orderId" to orderId,
                        "type" to "ORDER_STATUS_UPDATE"
                    )
                )
                backendApi.sendNotification(request)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error notifying customer: ${e.message}")
        }
    }

    suspend fun notifyDriversNewTask(orderId: String, pickupLoc: String, dropLoc: String, earnings: Double, distance: Double) {
        try {
            // Get all online drivers
            val driversSnapshot = firestore.collection("drivers")
                .whereEqualTo("isOnline", true)
                .get()
                .await()

            driversSnapshot.documents.forEach { doc ->
                val token = doc.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    val request = NotificationRequest(
                        targetToken = token,
                        title = "New Task Available!",
                        body = "Pickup at $pickupLoc",
                        data = mapOf(
                            "orderId" to orderId,
                            "type" to "NEW_RIDE_ALERT",
                            "pickupLoc" to pickupLoc,
                            "dropLoc" to dropLoc,
                            "earnings" to earnings.toString(),
                            "distance" to distance.toString()
                        )
                    )
                    backendApi.sendNotification(request)
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error notifying drivers: ${e.message}")
        }
    }
}
