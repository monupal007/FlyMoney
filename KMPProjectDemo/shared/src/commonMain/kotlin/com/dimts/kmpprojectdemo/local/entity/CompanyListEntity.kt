package com.dimts.kmpprojectdemo.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CompanyListEntity")
data class CompanyListEntity(
    @PrimaryKey val idTable: Int? = null,
    var id: Int? = null,
    var name: String? = null,
    var address: String? = null,
    var zip: String? = null,
    var country: String? = null,
    var employeeCount: Int? = null,
    var industry: String? = null,
    var marketCap: Long? = null,
    var domain: String? = null,
    var logo: String? = null,
    var ceoName: String? = null
)