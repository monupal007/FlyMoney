package com.dimts.gmcblChatBot.ui.screen.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dimts.gmcblChatBot.ui.constant.AppConstant
import com.dimts.gmcblChatBot.ui.screen.chat.ChatMessage
import com.dimts.gmcblChatBot.ui.screen.chat.ChatViewModel
import org.json.JSONObject

@Composable
fun DynamicChatView(
    message: ChatMessage,
    viewModel: ChatViewModel
) {

    val viewsArray = remember(message.text) {
        try {
            val rootObject = JSONObject(message.text)
            rootObject.optJSONArray(AppConstant.VIEWS)
        } catch (e: Exception) {
            null
        }
    }

    if (viewsArray == null) {
        Text("Something went wrong. Please try again in a moment", modifier = Modifier.padding(10.dp), color = Color.White)
        return
    }

    for (i in 0 until viewsArray.length()) {
        val viewObject = viewsArray.getJSONObject(i)
        RenderView(viewObject, message, viewModel)
    }
}