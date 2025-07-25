package com.dimts.kmpprojectdemo.android

import android.app.Application
import com.dimts.kmpprojectdemo.android.di.appModule
import com.dimts.kmpprojectdemo.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent

class MyApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
