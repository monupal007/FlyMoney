package com.laundryapp.admin

import android.app.Application
import com.laundryapp.admin.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AdminApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger()
            androidContext(this@AdminApplication)
        }
    }
}
