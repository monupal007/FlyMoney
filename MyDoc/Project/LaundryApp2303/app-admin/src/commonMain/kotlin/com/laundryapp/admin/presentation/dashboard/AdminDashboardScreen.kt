package com.laundryapp.admin.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToOrders: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToVendors: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToOfferApproval: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = koinViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout from the Admin panel?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Control Center", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Logout, "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            item {
                Text("Performance Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Revenue",
                            value = "₹${stats.totalRevenue.toInt()}",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = Color(0xFFE91E63)
                        )
                        SmallStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Orders",
                            value = stats.totalOrders.toString(),
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            color = Color(0xFF3F51B5)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Vendors",
                            value = stats.totalVendors.toString(),
                            icon = Icons.Default.Store,
                            color = Color(0xFF4CAF50)
                        )
                        SmallStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Fleet",
                            value = "${stats.onlineDrivers}/${stats.totalDrivers}",
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF2196F3)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Pending Tasks",
                            value = (stats.pendingVendors + stats.pendingOffers + stats.pendingDrivers).toString(),
                            icon = Icons.Default.PendingActions,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            item {
                Text("Quick Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ManagementActionItem(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "Order Management",
                        subtitle = "Track and manage all customer orders",
                        color = Color(0xFF3F51B5),
                        onClick = onNavigateToOrders
                    )
                    ManagementActionItem(
                        icon = Icons.Default.DryCleaning,
                        title = "Service Management",
                        subtitle = "Add, edit or remove laundry services",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToServices
                    )
                    ManagementActionItem(
                        icon = Icons.Default.LocalOffer,
                        title = "Offer Approvals",
                        subtitle = "${stats.pendingOffers} offers waiting for approval",
                        color = Color(0xFF9C27B0),
                        onClick = onNavigateToOfferApproval
                    )
                    ManagementActionItem(
                        icon = Icons.Default.LocalShipping,
                        title = "Driver Approvals",
                        subtitle = "${stats.pendingDrivers} drivers waiting for verification",
                        color = Color(0xFF607D8B),
                        onClick = onNavigateToDelivery
                    )
                    ManagementActionItem(
                        icon = Icons.Default.Store,
                        title = "Vendor Approvals",
                        subtitle = "${stats.pendingVendors} vendors pending approval",
                        color = Color(0xFF00BCD4),
                        onClick = onNavigateToVendors
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SmallStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ManagementActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.Default.ChevronRight, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
