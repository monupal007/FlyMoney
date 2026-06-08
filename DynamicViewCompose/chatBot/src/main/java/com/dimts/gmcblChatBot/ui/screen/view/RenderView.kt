package com.dimts.gmcblChatBot.ui.screen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dimts.gmcblChatBot.ui.constant.AppConstant
import com.dimts.gmcblChatBot.ui.screen.chat.ChatMessage
import com.dimts.gmcblChatBot.ui.screen.chat.ChatViewModel
import com.dimts.gmcblChatBot.ui.theme.LightBlue
import com.dimts.gmcblChatBot.ui.theme.themeColor
import org.json.JSONObject

@Composable
fun RenderView(
    viewObject: JSONObject,
    message: ChatMessage,
    viewModel: ChatViewModel
) {
    var isTypingFinished by remember { mutableStateOf(message.isTyped) }
    val textColor = Color.White // Bot message text color

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = LightBlue,
        contentColor = themeColor,
        disabledContainerColor = Color.White,
        disabledContentColor = Color.Gray
    )

    val buttonBorder = if (message.isActionable) null else BorderStroke(1.dp, Color.Gray)

    when (viewObject.optString(AppConstant.TYPE)) {

        AppConstant.COLUMN -> {
            val children = viewObject.optJSONArray(AppConstant.CHILDREN)

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                if (children != null) {
                    for (i in 0 until children.length()) {
                        RenderView(children.getJSONObject(i), message, viewModel)
                    }
                }
            }
        }

        AppConstant.ROW -> {
            val children = viewObject.optJSONArray(AppConstant.CHILDREN)

            Row(
                modifier = Modifier
            ) {
                if (children != null) {
                    for (i in 0 until children.length()) {
                        RenderView(children.getJSONObject(i), message, viewModel)
                    }
                }
            }
        }

        AppConstant.TEXT -> {
            if (message.isActionable && !message.isTyped) {
                TypewriterText(
                    text = viewObject.optString(AppConstant.MESSAGE),
                    modifier = Modifier.padding(10.dp),
                    color = textColor,
                    onFinish = {
                        isTypingFinished = true
                        message.isTyped = true
                    }
                )
            } else {
                Text(
                    text = viewObject.optString(AppConstant.MESSAGE),
                    modifier = Modifier.padding(10.dp),
                    color = textColor
                )
            }
        }

        AppConstant.COLUMN_TEXT -> {
            val optionsArray = viewObject.optJSONArray(AppConstant.OPTIONS)
            val options = mutableListOf<String>()

            if (optionsArray != null) {
                for (i in 0 until optionsArray.length()) {
                    options.add(optionsArray.optString(i))
                }
            }

            Column {
                options.forEach { option ->
                    if (message.isActionable && !message.isTyped) {
                        TypewriterText(
                            text = option,
                            modifier = Modifier.padding(10.dp),
                            color = textColor,
                            onFinish = { message.isTyped = true }
                        )
                    } else {
                        Text(
                            text = option,
                            modifier = Modifier.padding(10.dp),
                            color = textColor
                        )
                    }
                }
            }
        }

        AppConstant.ROW_TEXT -> {
            val optionsArray = viewObject.optJSONArray(AppConstant.OPTIONS)
            val options = mutableListOf<String>()

            if (optionsArray != null) {
                for (i in 0 until optionsArray.length()) {
                    options.add(optionsArray.optString(i))
                }
            }

            Row(
                modifier = Modifier
                    .width(250.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    if (message.isActionable && !message.isTyped) {
                        TypewriterText(
                            text = option,
                            modifier = Modifier.padding(10.dp),
                            color = textColor,
                            onFinish = { message.isTyped = true }
                        )
                    } else {
                        Text(
                            text = option,
                            modifier = Modifier.padding(10.dp),
                            color = textColor
                        )
                    }
                }
            }
        }

        AppConstant.IMAGE -> {
            // AsyncImage handling...
        }

        AppConstant.BUTTON -> {
            if (message.isActionable && !message.isTyped) {
                TypewriterText(
                    text = viewObject.optString(AppConstant.MESSAGE),
                    modifier = Modifier.padding(10.dp),
                    color = textColor,
                    onFinish = {
                        isTypingFinished = true
                        message.isTyped = true
                    }
                )
            } else {
                Text(
                    text = viewObject.optString(AppConstant.MESSAGE),
                    modifier = Modifier.padding(10.dp),
                    color = textColor
                )
                isTypingFinished = true
            }

            val optionsArray = viewObject.optJSONArray(AppConstant.OPTIONS)
            if (optionsArray != null && isTypingFinished) {
                Column {
                    for (i in 0 until optionsArray.length()) {
                        val option = optionsArray.optString(i)
                        Button(
                            enabled = message.isActionable,
                            onClick = { viewModel.sendMessage(option, message.id) },
                            modifier = Modifier
                                .width(250.dp)
                                .padding(2.dp),
                            colors = buttonColors,
                            border = buttonBorder
                        ) {
                            Text(option)
                        }
                    }
                }
            }
        }

        AppConstant.COLUMN_BUTTONS -> {
            val optionsArray = viewObject.optJSONArray(AppConstant.OPTIONS)
            val options = mutableListOf<String>()

            if (optionsArray != null) {
                for (i in 0 until optionsArray.length()) {
                    options.add(optionsArray.optString(i))
                }
            }

            Column {
                options.forEach { option ->
                    Button(
                        enabled = message.isActionable,
                        onClick = { viewModel.sendMessage(option, message.id) },
                        modifier = Modifier
                            .width(250.dp)
                            .padding(2.dp),
                        colors = buttonColors,
                        border = buttonBorder
                    ) {
                        Text(option)
                    }
                }
            }
        }

        AppConstant.ROW_BUTTONS -> {
            val optionsArray = viewObject.optJSONArray(AppConstant.OPTIONS)
            val options = mutableListOf<String>()

            if (optionsArray != null) {
                for (i in 0 until optionsArray.length()) {
                    options.add(optionsArray.optString(i))
                }
            }

            Row(
                modifier = Modifier
                    .width(250.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    Button(
                        enabled = message.isActionable,
                        onClick = { viewModel.sendMessage(option, message.id) },
                        modifier = Modifier
                            .padding(2.dp),
                        colors = buttonColors,
                        border = buttonBorder
                    ) {
                        Text(option)
                    }
                }
            }
        }

        else -> {
            Text("Unsupported Type", color = textColor)
        }
    }
}
