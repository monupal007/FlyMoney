package com.dimts.kmpprojectdemo.di

import androidx.room.RoomDatabase
import com.dimts.kmpprojectdemo.getDatabaseBuilder
import com.dimts.kmpprojectdemo.local.CompanyListDatabase
import org.koin.dsl.module

actual fun platformModule() = module {
    single<RoomDatabase.Builder<CompanyListDatabase>> {
        getDatabaseBuilder(get())
    }
}