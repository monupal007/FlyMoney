package com.dimts.kmpprojectdemo.di

import androidx.room.RoomDatabase
import com.dimts.kmpprojectdemo.local.CompanyListDatabase
import id.hidayatasep.bmitrackerv2.database.AppDatabase
import id.hidayatasep.bmitrackerv2.getDatabaseBuilder
import org.koin.dsl.module

actual fun platformModule() = module {
    single<RoomDatabase.Builder<CompanyListDatabase>> {
        getDatabaseBuilder()
    }
}