package com.laundryapp.customer.di

import com.laundryapp.core.di.commonModule
import com.laundryapp.core.di.platformModule
import com.laundryapp.customer.presentation.auth.LoginViewModel
import com.laundryapp.customer.navigation.NavViewModel
import com.laundryapp.customer.presentation.home.HomeViewModel
import com.laundryapp.customer.presentation.profile.ProfileViewModel
import com.laundryapp.customer.presentation.profile.AddressViewModel
import com.laundryapp.customer.presentation.order.OrderHistoryViewModel
import com.laundryapp.customer.presentation.order.OrderPlacementViewModel
import org.koin.core.module.Module
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::NavViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::AddressViewModel)
    viewModelOf(::OrderHistoryViewModel)
    viewModelOf(::OrderPlacementViewModel)
}

fun initKoin(appDeclaration: org.koin.core.context.GlobalContext.() -> Unit = {}) = 
    org.koin.core.context.startKoin {
        appDeclaration()
        modules(commonModule, platformModule, appModule)
    }
