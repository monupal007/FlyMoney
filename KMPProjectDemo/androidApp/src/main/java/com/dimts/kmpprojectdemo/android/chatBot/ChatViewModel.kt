package com.dimts.kmpprojectdemo.android.chatBot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimts.kmpprojectdemo.di.ChatGPTApi
import com.dimts.kmpprojectdemo.di.UiState
import com.dimts.kmpprojectdemo.repository.CompanyRepository
import io.ktor.http.ContentType.Application.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(private val apiService: CompanyRepository) : ViewModel() {

    private val _companyList = MutableStateFlow<UiState<String>>(UiState.Loading)
    val companyList: StateFlow<UiState<String>> = _companyList

    fun fetchMessage(chatGPT: ChatGPTApi, message : String) {
        viewModelScope.launch {
            _companyList.value = UiState.Loading
            try {
                val reply = chatGPT.getReply(message)
                _companyList.value = UiState.Success(reply)
            } catch (e: Exception) {
                _companyList.value = UiState.Error("Error: ${e.message}")
            }
        }
    }
}