package com.dimts.kmpprojectdemo.mapper

import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse
import com.dimts.kmpprojectdemo.local.entity.CompanyListEntity

object CompanyListMapper {

    fun map(input: CompanyListEntity): CompanyListResponse {
        return CompanyListResponse(
            id = input.id,
            name = input.name,
            address = input.address,
            zip = input.zip,
            country = input.country,
            employeeCount = input.employeeCount,
            industry = input.industry,
            marketCap = input.marketCap,
            domain = input.domain,
            logo = input.logo,
            ceoName = input.ceoName
        )
    }

    fun map(input: CompanyListResponse): CompanyListEntity {
        return CompanyListEntity(
            id = input.id,
            name = input.name,
            address = input.address,
            zip = input.zip,
            country = input.country,
            employeeCount = input.employeeCount,
            industry = input.industry,
            marketCap = input.marketCap,
            domain = input.domain,
            logo = input.logo,
            ceoName = input.ceoName
        )
    }
}