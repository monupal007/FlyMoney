package com.laundryapp.core.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ShowcaseState {
    private val targets = mutableStateMapOf<String, Rect>()
    var currentStep by mutableIntStateOf(-1)
    var isVisible by mutableStateOf(false)

    fun setTarget(key: String, coordinates: LayoutCoordinates) {
        val position = coordinates.positionInRoot()
        val size = coordinates.size
        targets[key] = Rect(position, Size(size.width.toFloat(), size.height.toFloat()))
    }

    fun getTarget(key: String): Rect? = targets[key]

    fun start() {
        currentStep = 0
        isVisible = true
    }

    fun dismiss() {
        isVisible = false
        currentStep = -1
    }
}

@Composable
fun rememberShowcaseState() = remember { ShowcaseState() }

data class ShowcaseStep(
    val key: String,
    val title: String,
    val description: String
)

@Composable
fun Showcase(
    state: ShowcaseState,
    steps: List<ShowcaseStep>,
    onComplete: () -> Unit
) {
    if (!state.isVisible || state.currentStep < 0 || state.currentStep >= steps.size) return

    val currentStepData = steps[state.currentStep]
    val targetRect = state.getTarget(currentStepData.key) ?: Rect.Zero
    val density = LocalDensity.current
    val holePadding = with(density) { 8.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .pointerInput(targetRect) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val position = event.changes.first().position
                        
                        // Check if the click is OUTSIDE the hole (including the visual padding)
                        val isOutsideHole = !targetRect.inflate(holePadding).contains(position)
                        
                        if (isOutsideHole) {
                            // Consume the event to prevent clicking other things
                            event.changes.forEach { it.consume() }
                        }
                        // If inside hole, we don't consume, allowing the event to pass to the UI below
                    }
                }
            }
    ) {
        // Overlay with hole
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.7f))
            
            if (targetRect != Rect.Zero) {
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        x = targetRect.left - holePadding,
                        y = targetRect.top - holePadding
                    ),
                    size = Size(
                        width = targetRect.width + holePadding * 2,
                        height = targetRect.height + holePadding * 2
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    blendMode = BlendMode.Clear
                )
            }
        }

        // Tooltip Content (remains non-clickable through)
        if (targetRect != Rect.Zero) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val screenHeight = constraints.maxHeight.toFloat()
                val isTargetInTopHalf = targetRect.center.y < screenHeight / 2
                
                Card(
                    modifier = Modifier
                        .align(if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter)
                        .padding(
                            bottom = if (isTargetInTopHalf) 100.dp else 0.dp, 
                            top = if (!isTargetInTopHalf) 100.dp else 0.dp
                        )
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = true) { /* Consume clicks on the card itself */ },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = currentStepData.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentStepData.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { state.dismiss() }) {
                                Text("Skip")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (state.currentStep < steps.size - 1) {
                                        state.currentStep++
                                    } else {
                                        state.dismiss()
                                        onComplete()
                                    }
                                }
                            ) {
                                Text(if (state.currentStep == steps.size - 1) "Finish" else "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.showcase(key: String, state: ShowcaseState): Modifier = this.onGloballyPositioned {
    state.setTarget(key, it)
}
