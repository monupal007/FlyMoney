package com.skyhighx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyhighx.ui.theme.SkyHighXTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkyHighXTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { ChatSection(viewModel) }
                ) { innerPadding ->
                    CrashGameScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CrashGameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "BALANCE", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = "$${String.format(Locale.US, "%.2f", viewModel.balance)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            Text(
                text = "SKY HIGH",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multiplier Display & Graph Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            GameGraph(multiplier = viewModel.multiplier, status = viewModel.status)
            
            val multiplierColor by animateColorAsState(
                targetValue = when (viewModel.status) {
                    GameStatus.CRASHED -> Color.Red
                    GameStatus.RUNNING -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.onSurface
                }, label = "multiplierColor"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format(Locale.US, "%.2f", viewModel.multiplier)}x",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = multiplierColor
                )
                if (viewModel.status == GameStatus.CRASHED) {
                    Text(text = "CRASHED", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.history) { point ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (point >= 2.0) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE91E63).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.2f", point)}x",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (point >= 2.0) Color(0xFF2E7D32) else Color(0xFFC2185B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "BET AMOUNT", style = MaterialTheme.typography.labelLarge)
                    Text(text = "$${viewModel.currentBet.toInt()}", fontWeight = FontWeight.Bold)
                }
                
                Slider(
                    value = viewModel.currentBet.toFloat(),
                    onValueChange = { viewModel.updateBet(it.toDouble()) },
                    valueRange = 10f..500f,
                    steps = 49,
                    enabled = viewModel.status != GameStatus.RUNNING
                )

                Button(
                    onClick = {
                        if (viewModel.status == GameStatus.RUNNING) viewModel.cashOut()
                        else viewModel.startGame()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.status == GameStatus.RUNNING) Color(0xFFFF9800) else Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = if (viewModel.status == GameStatus.RUNNING) "CASH OUT" else "PLACE BET",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun GameGraph(multiplier: Double, status: GameStatus) {
    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        val width = size.width
        val height = size.height
        
        val progress = ((multiplier - 1.0) / 4.0).coerceIn(0.0, 1.2).toFloat()
        val endX = width * progress.coerceAtMost(1f)
        val endY = height - (height * (progress * 0.8f).coerceAtMost(1f))

        val path = Path().apply {
            moveTo(0f, height)
            quadraticTo(width * progress * 0.4f, height, endX, endY)
        }

        drawPath(
            path = path,
            color = if (status == GameStatus.CRASHED) Color.Red else Color(0xFF4CAF50),
            style = Stroke(width = 10f)
        )

        // Draw "Rocket" (Simple Triangle/Shape)
        if (status != GameStatus.IDLE) {
            translate(left = endX, top = endY) {
                val rocketPath = Path().apply {
                    moveTo(0f, -15f)
                    lineTo(-10f, 15f)
                    lineTo(10f, 15f)
                    close()
                }
                rotate(degrees = 45f) {
                    drawPath(
                        path = rocketPath,
                        color = if (status == GameStatus.CRASHED) Color.Red else Color.Yellow
                    )
                }
            }
        }
    }
}

@Composable
fun ChatSection(viewModel: GameViewModel) {
    var text by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Text("LIVE CHAT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(viewModel.chatMessages.reversed()) { msg ->
                Text(
                    text = "${msg.user}: ${msg.message}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Say something...") },
                textStyle = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = { 
                if (text.isNotBlank()) {
                    viewModel.sendMessage(text)
                    text = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
