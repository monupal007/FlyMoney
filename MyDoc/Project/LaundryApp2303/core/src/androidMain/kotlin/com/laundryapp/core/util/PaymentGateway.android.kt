package com.laundryapp.core.util

import android.app.Activity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class AndroidPaymentGateway(private val activity: Activity) : PaymentGateway, PaymentResultListener {
    private var currentCallback: PaymentCallback? = null

    override fun startPayment(
        amount: Double,
        orderId: String,
        customerEmail: String,
        customerPhone: String,
        callback: PaymentCallback
    ) {
        currentCallback = callback
        val checkout = Checkout()
        // The key should ideally be in the AndroidManifest.xml as metadata
        // <meta-data android:name="com.razorpay.ApiKey" android:value="YOUR_KEY" />
        
        try {
            val options = JSONObject()
            options.put("name", "Laundry App")
            options.put("description", "Order #$orderId")
            options.put("theme.color", "#1565C0")
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // Amount in paise
            options.put("prefill.email", customerEmail)
            options.put("prefill.contact", customerPhone)

            checkout.open(activity, options)
        } catch (e: Exception) {
            callback.onError(0, e.message ?: "Error in starting Razorpay Checkout")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        currentCallback?.onSuccess(razorpayPaymentId ?: "")
        currentCallback = null
    }

    override fun onPaymentError(code: Int, response: String?) {
        currentCallback?.onError(code, response ?: "Unknown error")
        currentCallback = null
    }
}

actual val platformPaymentGateway: PaymentGateway
    get() = throw Exception("Use Koin to inject the platform implementation")
