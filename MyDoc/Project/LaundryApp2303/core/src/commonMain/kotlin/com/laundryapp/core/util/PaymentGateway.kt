package com.laundryapp.core.util

interface PaymentCallback {
    fun onSuccess(paymentId: String)
    fun onError(code: Int, message: String)
}

interface PaymentGateway {
    fun startPayment(
        amount: Double,
        orderId: String,
        customerEmail: String,
        customerPhone: String,
        callback: PaymentCallback
    )
}

expect val platformPaymentGateway: PaymentGateway
