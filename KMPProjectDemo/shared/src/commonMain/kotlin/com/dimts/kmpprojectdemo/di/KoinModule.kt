package com.dimts.kmpprojectdemo.di

import com.dimts.kmpprojectdemo.local.getRoomDatabase
import com.dimts.kmpprojectdemo.local.getUserGrowthDao
import com.dimts.kmpprojectdemo.repository.CompanyRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

val commonModule = module {
    single { getHttpClient() } // Uses platform-specific engine
    single { ApiService(get()) }
    single { CompanyRepository(get(), get()) }
    single { getRoomDatabase(get()) }
    single { getUserGrowthDao(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(
        commonModule,
        platformModule()
    )
}