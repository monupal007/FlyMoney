package com.dimts.kmpprojectdemo.di

import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse

interface APIInterface {
    suspend fun fetch(): ApiResponse
    suspend fun fetchCompanyList(): ArrayList<CompanyListResponse>
}