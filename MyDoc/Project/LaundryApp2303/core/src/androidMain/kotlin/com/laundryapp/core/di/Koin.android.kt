package com.laundryapp.core.di

import com.laundryapp.core.util.AndroidLocationManager
import com.laundryapp.core.util.AndroidPaymentGateway
import com.laundryapp.core.util.LocationManager
import com.laundryapp.core.util.PaymentGateway
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

actual val platformModule = module {
    single { OkHttp.create() }
    single<LocationManager> { AndroidLocationManager(get()) }
    single<PaymentGateway> { AndroidPaymentGateway(get()) }
}
