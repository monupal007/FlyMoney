package com.maka.flymoney.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoundHistoryBar(
    history: List<Double>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1A))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true
    ) {
        items(history) { multiplier ->
            MultiplierPill(multiplier)
        }
    }
}

@Composable
fun MultiplierPill(multiplier: Double) {
    val backgroundColor = when {
        multiplier < 2.0 -> Color(0xFF34344A)
        multiplier < 10.0 -> Color(0xFF00FF88).copy(alpha = 0.2f)
        else -> Color(0xFFFFCC00).copy(alpha = 0.2f)
    }
    
    val textColor = when {
        multiplier < 2.0 -> Color(0xFF8888AA)
        multiplier < 10.0 -> Color(0xFF00FF88)
        else -> Color(0xFFFFCC00)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${String.format("%.2f", multiplier)}x",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
