package com.dimts.kmpprojectdemo.android.companyList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimts.kmpprojectdemo.di.ApiResponse
import com.dimts.kmpprojectdemo.di.UiState
import com.dimts.kmpprojectdemo.di.companyList.CompanyListResponse
import com.dimts.kmpprojectdemo.repository.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(private val apiService: CompanyRepository) : ViewModel() {

    private val _news = MutableStateFlow<UiState<ApiResponse>>(UiState.Loading)
    val news: StateFlow<UiState<ApiResponse>> = _news

    private val _companyList = MutableStateFlow<UiState<List<CompanyListResponse>>>(UiState.Loading)
    val companyList: StateFlow<UiState<List<CompanyListResponse>>> = _companyList

//    fun fetchNews() {
//        viewModelScope.launch {
//            _news.value = UiState.Loading
//            try {
//                val result = apiService.fetch()
//                _news.value = UiState.Success(result)
//            } catch (e: Exception) {
//                _news.value = UiState.Error("Error: ${e.message}")
//            }
//        }
//    }

    fun fetchCompanyList() {
        viewModelScope.launch {
            _companyList.value = UiState.Loading
            try {
                val result = apiService.getCompanyList()
                _companyList.value = UiState.Success(result)
            } catch (e: Exception) {
                _companyList.value = UiState.Error("Error: ${e.message}")
            }
        }
    }
}