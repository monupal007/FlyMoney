package com.maka.flymoney.presentation.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maka.flymoney.domain.model.RoundState
import java.util.Locale
import kotlin.math.pow

@Composable
fun CurveCanvas(
    multiplier: Double,
    roundState: RoundState,
    countdown: Int,
    modifier: Modifier = Modifier
) {
    val curveColor by animateColorAsState(
        targetValue = when (roundState) {
            RoundState.RUNNING -> Color(0xFF00FF88)
            RoundState.CRASHED -> Color(0xFFFF4444)
            else -> Color.Transparent
        }, label = "curveColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (roundState == RoundState.WAITING) {
            Text(
                text = "NEXT ROUND IN\n${countdown}s",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        var planeOffset by remember { mutableStateOf(Offset.Zero) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            if (roundState == RoundState.RUNNING || roundState == RoundState.CRASHED) {
                val path = Path()
                path.moveTo(0f, height)

                // Scaling factor to make the curve fill the screen gradually
                val curveProgress = ((multiplier - 1.0) / 10.0).coerceIn(0.0, 1.0).toFloat()

                val points = 100
                val currentX = curveProgress * width
                var lastX = 0f
                var lastY = height

                for (i in 0..points) {
                    val t = i.toFloat() / points
                    val x = t * width
                    val y = height - (height * (1.1f.pow(i.toFloat()) - 1) / (1.1f.pow(points.toFloat()) - 1))
                    
                    if (x <= currentX) {
                        path.lineTo(x, y)
                        lastX = x
                        lastY = y
                    } else {
                        break
                    }
                }
                
                planeOffset = Offset(lastX, lastY)

                // Draw gradient under the curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(lastX, height)
                    lineTo(0f, height)
                    close()
                }
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(curveColor.copy(alpha = 0.3f), Color.Transparent),
                        startY = lastY,
                        endY = height
                    )
                )

                drawPath(
                    path = path,
                    color = curveColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Plane Icon at the tip (Using PlayArrow as a fallback for airplane if extended icons are missing)
        if (roundState == RoundState.RUNNING) {
            val density = LocalContext.current.resources.displayMetrics.density
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFFFF4444),
                    modifier = Modifier
                        .offset(
                            x = (planeOffset.x / density).dp - 16.dp,
                            y = (planeOffset.y / density).dp - 16.dp
                        )
                        .size(32.dp)
                        .rotate(-45f)
                )
            }
        }

        if (roundState == RoundState.RUNNING || roundState == RoundState.CRASHED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%.2f", multiplier) + "x",
                    color = if (roundState == RoundState.CRASHED) Color.Red else Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )
                if (roundState == RoundState.CRASHED) {
                    Text(
                        text = "FLEW AWAY!",
                        color = Color.Red,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
