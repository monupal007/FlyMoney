package com.dimts.kmpprojectdemo.android.di

import com.dimts.kmpprojectdemo.android.chatBot.ChatViewModel
import com.dimts.kmpprojectdemo.android.companyList.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::ChatViewModel)
}