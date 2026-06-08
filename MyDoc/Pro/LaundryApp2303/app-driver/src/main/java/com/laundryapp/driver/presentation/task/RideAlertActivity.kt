package com.laundryapp.driver.presentation.task

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laundryapp.core.ui.theme.LaundryAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RideAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        turnScreenOnAndKeyguardOff()
        enableEdgeToEdge()

        val orderId = intent.getStringExtra("orderId") ?: ""
        val pickupLoc = intent.getStringExtra("pickupLoc") ?: "Unknown"
        val dropLoc = intent.getStringExtra("dropLoc") ?: "Unknown"
        val distance = intent.getStringExtra("distance") ?: "0 km"
        val earnings = intent.getStringExtra("earnings") ?: "₹0"

        setContent {
            LaundryAppTheme {
                val viewModel: RideAlertViewModel = hiltViewModel()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    viewModel.eventFlow.collect { event ->
                        when (event) {
                            is RideAlertViewModel.UiEvent.Success -> {
                                Toast.makeText(context, "Ride Accepted!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            is RideAlertViewModel.UiEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                RideAlertScreen(
                    pickupLoc = pickupLoc,
                    dropLoc = dropLoc,
                    distance = distance,
                    earnings = earnings,
                    onAccept = { viewModel.acceptRide(orderId) },
                    onReject = { 
                        viewModel.rejectRide() 
                        finish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@RideAlertActivity, null)
            }
        }
    }
}

@Composable
fun RideAlertScreen(
    pickupLoc: String,
    dropLoc: String,
    distance: String,
    earnings: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(48.dp))
                
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(20.dp).fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "New Ride Request",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    LocationItem(
                        icon = Icons.Default.LocationOn,
                        iconTint = Color(0xFF4CAF50),
                        label = "PICKUP",
                        address = pickupLoc
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(start = 11.dp)
                            .height(30.dp)
                            .width(2.dp)
                            .background(Color.LightGray)
                    )
                    
                    LocationItem(
                        icon = Icons.Default.LocationOn,
                        iconTint = Color(0xFFF44336),
                        label = "DROP",
                        address = dropLoc
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoColumn(label = "DISTANCE", value = distance)
                        InfoColumn(label = "EST. EARNINGS", value = earnings, valueColor = Color(0xFF2E7D32))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Reject", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Accept", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        IconButton(
            onClick = onReject,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun LocationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, label: String, address: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(address, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2)
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String, valueColor: Color = Color.Black) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
