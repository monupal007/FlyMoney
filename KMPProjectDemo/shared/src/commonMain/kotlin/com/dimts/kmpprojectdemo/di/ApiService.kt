package com.dimts.kmpprojectdemo.di

import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Single


@Single
class ApiService(private val client: HttpClient) : APIInterface {

    override suspend fun fetch(): ApiResponse {
        val url = "https://jsonplaceholder.typicode.com/posts/1"
        return client.get(url).body()
    }

    override suspend fun fetchCompanyList(): ArrayList<CompanyListResponse> {
        val raw = "https://fake-json-api.mock.beeceptor.com/companies"
        return client.get(raw).body()
    }
}