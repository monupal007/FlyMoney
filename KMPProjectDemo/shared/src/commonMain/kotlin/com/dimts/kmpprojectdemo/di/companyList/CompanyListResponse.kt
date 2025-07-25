package com.dimts.kmpprojectdemo.di.companyList

import kotlinx.serialization.Serializable

@Serializable
data class CompanyListResponse(
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
