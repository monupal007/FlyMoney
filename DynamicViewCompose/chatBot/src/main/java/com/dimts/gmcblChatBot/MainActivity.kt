package com.dimts.gmcblChatBot

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dimts.gmcblChatBot.di.ApiService
import com.dimts.gmcblChatBot.ui.screen.ChatScreen
import com.dimts.gmcblChatBot.ui.screen.chat.ChatRepositoryImpl
import com.dimts.gmcblChatBot.ui.screen.chat.ChatViewModel
import com.dimts.gmcblChatBot.ui.screen.chat.ChatViewModelFactory
import com.dimts.gmcblChatBot.ui.screen.chat.SendMessageUseCase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val apiService = ApiService()
        val repository = ChatRepositoryImpl(apiService)
        val useCase = SendMessageUseCase(repository)
        val factory = ChatViewModelFactory(useCase,deviceId)


        setContent {
            val viewModel: ChatViewModel = viewModel(factory = factory)
            ChatScreen(viewModel)
        }
    }
}

