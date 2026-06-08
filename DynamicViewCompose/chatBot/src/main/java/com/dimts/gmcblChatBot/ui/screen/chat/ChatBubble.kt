package com.dimts.gmcblChatBot.ui.screen.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dimts.gmcblChatBot.ui.screen.view.DynamicChatView
import com.dimts.gmcblChatBot.ui.theme.LightBlue
import com.dimts.gmcblChatBot.ui.theme.themeColor

@Composable
fun ChatBubble(
    message: ChatMessage,
    viewModel: ChatViewModel
) {

    val isUser = message.type == MessageType.USER

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        // 👤 Show profile icon ONLY for BOT
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Bot",
                tint = themeColor,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 6.dp)
            )
        }

        Surface(
            shape = if (isUser) {
                // User bubble shape
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
            } else {
                // Bot bubble shape
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 16.dp
                )
            },
            color = if (isUser) LightBlue else themeColor
        ) {

            Box(modifier = Modifier.padding(0.dp)) {
                if (!isUser) {
                    DynamicChatView(
                        message = message,
                        viewModel = viewModel
                    )
                } else {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(10.dp),
                        color = Color.Black
                    )
                }
            }
        }
    }
}
