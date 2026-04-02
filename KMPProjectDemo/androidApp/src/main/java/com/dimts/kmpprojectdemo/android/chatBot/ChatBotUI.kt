package com.dimts.kmpprojectdemo.android.chatBot


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dimts.kmpprojectdemo.di.ChatGPTApi
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatBotUI() {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Bot: Hello! Ask me anything...") }
    val viewModel: ChatViewModel = koinViewModel()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
            reverseLayout = true
        ) {
            items(messages.size) { index ->
                Text(messages[messages.size - 1 - index])
            }
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .imePadding()) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        messages.add("You: $userInput")
                        val input = userInput
                        userInput = ""
//                        onSend(input) { reply -> messages.add(reply) }
                        viewModel.fetchMessage(
                            ChatGPTApi("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"),
                            input
                        )
                        messages.add("Bot : ${viewModel.companyList.value}")

                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}