package com.dimts.gmcblChatBot.ui.screen.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    wordDelayMillis: Long = 100L,
    lineDelayMillis: Long = 300L,
    onFinish: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        displayedText = ""
        val lines = text.split("\n")
        lines.forEachIndexed { lineIndex, line ->
            val words = line.split(" ")
            words.forEachIndexed { wordIndex, word ->
                displayedText += if (wordIndex == 0 && lineIndex == 0) word else if (wordIndex == 0) "\n$word" else " $word"
                delay(wordDelayMillis)
            }
            if (lineIndex < lines.size - 1) {
                delay(lineDelayMillis)
            }
        }
        onFinish()
    }

    Text(
        text = displayedText,
        modifier = modifier,
        color = color
    )
}
