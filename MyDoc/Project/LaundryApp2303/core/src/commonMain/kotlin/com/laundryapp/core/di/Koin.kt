package com.laundryapp.core.di

import com.laundryapp.core.data.remote.OpenRouterApi
import com.laundryapp.core.data.repository.*
import com.laundryapp.core.presentation.driver.auth.DriverLoginViewModel
import com.laundryapp.core.presentation.driver.auth.DriverRegistrationViewModel
import com.laundryapp.core.presentation.driver.dashboard.DriverDashboardViewModel
import com.laundryapp.core.presentation.driver.map.DriverMapViewModel
import com.laundryapp.core.presentation.driver.profile.DriverProfileViewModel
import com.laundryapp.core.presentation.driver.task.TaskDetailViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    single { OpenRouterApi(get()) }
    
    // Repositories
    single { AuthRepository() }
    single { OrderRepository() }
    single { DriverRepository(firestore = get(), imageUploader = get()) }
    single { VendorRepository(firestore = get(), imageUploader = get()) }
    single { ServiceRepository() }

    // ViewModels
    viewModel { DriverDashboardViewModel(get(), get(), get(), get()) }
    viewModel { TaskDetailViewModel(get(), get()) }
    viewModel { DriverRegistrationViewModel(get(), get()) }
    viewModel { DriverLoginViewModel(get()) }
    viewModel { DriverProfileViewModel(get(), get()) }
    viewModel { DriverMapViewModel(get(), get(), get()) }
}

expect val platformModule: Module
