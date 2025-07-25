package com.dimts.kmpprojectdemo.repository

import com.dimts.kmpprojectdemo.di.ApiService
import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse
import com.dimts.kmpprojectdemo.local.CompanyListDao
import com.dimts.kmpprojectdemo.mapper.CompanyListMapper
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single
class CompanyRepository(
    private val remote: ApiService,
    private val local: CompanyListDao
) : KoinComponent {
    suspend fun getCompanyList(): List<CompanyListResponse> {
        return try {
            val companies = remote.fetchCompanyList()
            local.deleteCompanyList(companies.map { CompanyListMapper.map(it) })
            local.insertCompanyList(companies.map { CompanyListMapper.map(it) })
            companies
        } catch (e: Exception) {
            local.getCompanyList().map { CompanyListMapper.map(it) }
        } as List<CompanyListResponse>
    }
}