package com.dimts.kmpprojectdemo.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dimts.kmpprojectdemo.local.entity.CompanyListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [CompanyListEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class CompanyListDatabase : RoomDatabase() {
    abstract fun companyListDao(): CompanyListDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<CompanyListDatabase>{
    override fun initialize(): CompanyListDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<CompanyListDatabase>
): CompanyListDatabase {
    return builder
        .addMigrations()
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

fun getUserGrowthDao(appDatabase: CompanyListDatabase) = appDatabase.companyListDao()





