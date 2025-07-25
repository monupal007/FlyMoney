package com.dimts.kmpprojectdemo.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dimts.kmpprojectdemo.local.entity.CompanyListEntity

@Dao
interface CompanyListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyList(items: List<CompanyListEntity>)

    @Delete
    suspend fun deleteCompanyList(items: List<CompanyListEntity>)

    @Query("SELECT * FROM CompanyListEntity")
    suspend fun getCompanyList(): List<CompanyListEntity>
}