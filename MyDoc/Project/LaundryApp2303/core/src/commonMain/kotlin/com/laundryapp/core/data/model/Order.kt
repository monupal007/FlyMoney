package com.laundryapp.core.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    PLACED, 
    CONFIRMED,
    ACCEPTED_BY_VENDOR,
    REJECTED_BY_VENDOR,
    PICKUP_ASSIGNED,
    PICKED_UP_FROM_CUSTOMER,
    PICKED_UP,
    RECEIVED_BY_VENDOR,
    PROCESSING, 
    IN_PROGRESS,
    READY_FOR_DELIVERY, 
    READY,
    DELIVERY_ASSIGNED,
    PICKED_UP_FROM_VENDOR,
    OUT_FOR_DELIVERY, 
    DELIVERED, 
    CANCELLED
}

@Serializable
enum class PaymentStatus { PENDING, PAID, FAILED, REFUNDED }

@Serializable
data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: OrderStatus = OrderStatus.PLACED,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val totalAmount: Double = 0.0,
    val commissionAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val pickupFee: Double = 0.0,
    val platformFee: Double = 0.0,
    val vendorEarning: Double = 0.0,
    val adminEarning: Double = 0.0,
    val distance: Double = 0.0,
    val baseFare: Double = 20.0,
    val perKmRate: Double = 5.0,
    val driverEarning: Double = 0.0,
    val pickupAddress: Address = Address(),
    val deliveryAddress: Address = Address(),
    val scheduledPickup: Long = 0L,
    val scheduledDelivery: Long = 0L,
    val pickupDriverId: String = "",
    val pickupDriverName: String = "",
    val pickupDriverPhone: String = "",
    val deliveryDriverId: String = "",
    val deliveryDriverName: String = "",
    val deliveryDriverPhone: String = "",
    val assignedDriverName: String = "",
    val specialInstructions: String = "",
    val paymentId: String = "",
    val statusHistory: List<StatusUpdate> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val appliedOfferId: String? = null,
    val discountAmount: Double = 0.0,
    val vendorRating: Float? = null,
    val vendorReview: String? = null,
    val pickupDriverRating: Float? = null,
    val pickupDriverReview: String? = null,
    val deliveryDriverRating: Float? = null,
    val deliveryDriverReview: String? = null,
    val driverRating: Float? = null,
    val driverReview: String? = null
)

@Serializable
data class OrderItem(
    val serviceId: String = "",
    val serviceName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "kg",
    val pricePerUnit: Double = 0.0,
    val subtotal: Double = 0.0
)

@Serializable
data class StatusUpdate(
    val status: OrderStatus = OrderStatus.PLACED,
    val timestamp: Long = 0L,
    val note: String = ""
)
