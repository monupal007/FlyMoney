package com.dimts.gmcblChatBot.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimts.gmcblChatBot.ui.screen.chat.ChatBubble
import com.dimts.gmcblChatBot.ui.screen.chat.ChatViewModel
import com.dimts.gmcblChatBot.ui.screen.chat.VoiceRecognizer
import com.dimts.gmcblChatBot.ui.theme.themeColor

@Composable
fun ChatScreen(viewModel: ChatViewModel) {

    val state by viewModel.state.collectAsState()
    var text by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val voiceRecognizer = remember {
        VoiceRecognizer(
            context = context,
            onListeningStateChanged = { listening ->
                isListening = listening
            },
            onResult = { spokenText ->
                text = spokenText
                // Only send if the bot isn't currently thinking
                if (!viewModel.state.value.isTyping) {
                    viewModel.sendMessage(spokenText)
                    text = ""
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecognizer.stopListening()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                voiceRecognizer.startListening()
            } else {
                Toast.makeText(
                    context,
                    "Microphone permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
            hasPermission = granted
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "GMCBL Bot",
                    color = themeColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color.Green, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        color = themeColor.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Chat Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(
                items = state.messages.reversed(),
                key = { it.id }
            ) { message ->
                ChatBubble(message, viewModel)
            }
        }

        if (state.isTyping) {
            Text(
                "Bot is thinking...",
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
        }

        // Bottom Input Container
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(Color(0xFFF1F1F1), RoundedCornerShape(30.dp))
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Text Field - Always enabled as requested
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(if (isListening) "Listening..." else "Type a message...") 
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Mic Button - Disabled while bot is thinking
                IconButton(
                    enabled = !state.isTyping,
                    onClick = {
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                voiceRecognizer.stopListening()
                            } else {
                                voiceRecognizer.startListening()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (state.isTyping) Color.Gray else if (isListening) Color.Red else themeColor
                    )
                }

                // Send/Stop Button - Block sending while typing by showing Stop
                IconButton(
                    onClick = {
                        if (state.isTyping) {
                            viewModel.stopMessage()
                        } else if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                            text = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (state.isTyping) Icons.Default.Stop else Icons.Default.Send,
                        contentDescription = if (state.isTyping) "Stop" else "Send",
                        tint = if (state.isTyping) Color.Red else themeColor
                    )
                }
            }
        }
    }
}