package com.laundryapp.admin.di

import com.laundryapp.admin.navigation.AdminNavViewModel
import com.laundryapp.admin.presentation.auth.AdminLoginViewModel
import com.laundryapp.admin.presentation.customers.CustomerManagementViewModel
import com.laundryapp.admin.presentation.dashboard.AdminDashboardViewModel
import com.laundryapp.admin.presentation.delivery.DriverManagementViewModel
import com.laundryapp.admin.presentation.orders.AdminOrderDetailViewModel
import com.laundryapp.admin.presentation.orders.AdminOrdersViewModel
import com.laundryapp.admin.presentation.services.AdminOfferApprovalViewModel
import com.laundryapp.admin.presentation.services.ServiceManagementViewModel
import com.laundryapp.admin.presentation.vendors.VendorDetailViewModel
import com.laundryapp.admin.presentation.vendors.VendorManagementViewModel
import com.laundryapp.core.di.commonModule
import com.laundryapp.core.di.platformModule
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::AdminNavViewModel)
    viewModelOf(::AdminLoginViewModel)
    viewModelOf(::AdminDashboardViewModel)
    viewModelOf(::AdminOrdersViewModel)
    viewModelOf(::AdminOrderDetailViewModel)
    viewModelOf(::CustomerManagementViewModel)
    viewModelOf(::VendorManagementViewModel)
    viewModelOf(::VendorDetailViewModel)
    viewModelOf(::DriverManagementViewModel)
    viewModelOf(::ServiceManagementViewModel)
    viewModelOf(::AdminOfferApprovalViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = 
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule, appModule)
    }
