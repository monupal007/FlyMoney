package com.dimts.kmpprojectdemo

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dimts.kmpprojectdemo.local.CompanyListDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<CompanyListDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<CompanyListDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}