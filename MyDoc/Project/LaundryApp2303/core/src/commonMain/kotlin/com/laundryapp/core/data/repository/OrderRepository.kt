package com.laundryapp.core.data.repository

import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.data.model.StatusUpdate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val orderCollection = firestore.collection("orders")

    suspend fun placeOrder(order: Order): Result<Unit> = runCatching {
        orderCollection.document(order.id).set(order)
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return orderCollection.document(orderId).snapshots.map { it.data<Order>() }
    }

    fun getOrdersByUserId(userId: String): Flow<List<Order>> {
        return orderCollection.where { "userId" equalTo userId }.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<Order>() }
        }
    }

    fun getAllOrders(): Flow<List<Order>> {
        return orderCollection.snapshots.map { snapshot: QuerySnapshot ->
            snapshot.documents.map { it.data<Order>() }
        }
    }

    fun getDriverOrders(driverId: String): Flow<List<Order>> {
        return orderCollection.snapshots.map { snapshot ->
            snapshot.documents.map { it.data<Order>() }.filter { 
                it.pickupDriverId == driverId || it.deliveryDriverId == driverId
            }
        }
    }

    fun getUnassignedTasks(): Flow<List<Order>> {
        return orderCollection.snapshots.map { snapshot ->
            snapshot.documents.map { it.data<Order>() }.filter {
                (it.status == OrderStatus.ACCEPTED_BY_VENDOR && it.pickupDriverId.isEmpty()) ||
                (it.status == OrderStatus.READY_FOR_DELIVERY && it.deliveryDriverId.isEmpty())
            }
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> = runCatching {
        val statusUpdate = StatusUpdate(status = status, timestamp = 0L)
        val doc = orderCollection.document(orderId)
        val currentOrder = doc.get().data<Order>()
        val history = (currentOrder?.statusHistory ?: emptyList()) + statusUpdate
        doc.update("status" to status, "statusHistory" to history, "updatedAt" to 0L)
    }

    suspend fun assignDriver(orderId: String, driverId: String, driverName: String, isPickup: Boolean, driverPhone: String): Result<Unit> = runCatching {
        val updates = if (isPickup) {
            mapOf(
                "pickupDriverId" to driverId,
                "pickupDriverName" to driverName,
                "pickupDriverPhone" to driverPhone,
                "status" to OrderStatus.PICKUP_ASSIGNED
            )
        } else {
            mapOf(
                "deliveryDriverId" to driverId,
                "deliveryDriverName" to driverName,
                "deliveryDriverPhone" to driverPhone,
                "status" to OrderStatus.DELIVERY_ASSIGNED
            )
        }
        orderCollection.document(orderId).update(updates)
    }
}
