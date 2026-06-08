package com.laundryapp.driver.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laundryapp.core.data.model.Address
import com.laundryapp.core.data.model.LatLngData
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.presentation.driver.dashboard.DriverDashboardViewModel
import com.laundryapp.core.presentation.driver.dashboard.DriverTask
import com.laundryapp.core.ui.components.LogoutConfirmationDialog
import com.laundryapp.core.util.LocationUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    onLogout: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSeeAllCompleted: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: DriverDashboardViewModel = koinViewModel(),
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    onPermissionRequest: () -> Unit
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val unassignedTasks by viewModel.unassignedTasks.collectAsState()
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val message by viewModel.message.collectAsState()
    val driverLocation by viewModel.driverLocation.collectAsState()
    val vendorLocations by viewModel.vendorLocations.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Driver Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text(if (isOnline) "You are online" else "You are offline", 
                             style = MaterialTheme.typography.bodySmall, 
                             color = if (isOnline) Color(0xFF2E7D32) else Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person, 
                                    "Profile", 
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && activeTasks.isEmpty() && unassignedTasks.isEmpty() && completedTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Location/GPS Banner
                if (!hasLocationPermission || !isGpsEnabled) {
                    item {
                        LocationServiceBanner(
                            hasPermission = hasLocationPermission,
                            isGpsEnabled = isGpsEnabled,
                            onEnable = onPermissionRequest
                        )
                    }
                }

                // Stats Summary (Earnings, Active, Completed)
                item {
                    DriverDashboardStats(
                        totalEarnings = totalEarnings,
                        activeTasks = activeTasks.size,
                        completedTasks = completedTasks.size
                    )
                }

                // Online/Offline status card
                if (!isOnline) {
                    item {
                        OfflineBanner(onGoToProfile = onNavigateToProfile)
                    }
                }

                // Available tasks section
                if (isOnline && hasLocationPermission && isGpsEnabled) {
                    item {
                        SectionHeader(
                            title = "New Available Tasks", 
                            count = unassignedTasks.size, 
                            icon = Icons.Default.Explore,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (unassignedTasks.isEmpty()) {
                        item {
                            EmptySectionPlaceholder(
                                text = "No new tasks nearby",
                                icon = Icons.Default.Radar
                            )
                        }
                    } else {
                        items(unassignedTasks) { task ->
                            AvailableTaskCard(
                                task = task, 
                                onAccept = { viewModel.acceptTask(task) },
                                onReject = { viewModel.rejectTask(task.id) },
                                driverLocation = driverLocation,
                                vendorLocation = vendorLocations[task.vendorId]
                            )
                        }
                    }
                }

                // Active tasks section
                item {
                    SectionHeader(
                        title = "Ongoing Tasks", 
                        count = activeTasks.size, 
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        color = Color(0xFF2E7D32)
                    )
                }
                
                if (activeTasks.isEmpty()) {
                    item {
                        EmptySectionPlaceholder(
                            text = "You don't have any active tasks",
                            icon = Icons.AutoMirrored.Filled.Assignment
                        )
                    }
                } else {
                    items(activeTasks, key = { it.id }) { task ->
                        ActiveTaskListItem(
                            task = task, 
                            onClick = { onTaskClick(task.order.id) },
                            vendorLocation = vendorLocations[task.order.vendorId]
                        )
                    }
                }

                // Completed tasks section
                if (completedTasks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(
                                title = "Recently Completed", 
                                count = completedTasks.size, 
                                icon = Icons.Default.TaskAlt,
                                color = Color.Gray
                            )
                            if (completedTasks.size > 4) {
                                TextButton(onClick = onSeeAllCompleted) {
                                    Text("See All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    
                    items(completedTasks.take(4), key = { it.id }) { task ->
                        CompletedTaskListItem(task = task)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun DriverDashboardStats(
    totalEarnings: Double,
    activeTasks: Int,
    completedTasks: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBoxSmall(
            modifier = Modifier.weight(1f),
            label = "Earnings",
            value = "₹${totalEarnings.toInt()}",
            icon = Icons.Default.Payments,
            color = Color(0xFF2E7D32)
        )
        StatBoxSmall(
            modifier = Modifier.weight(1f),
            label = "Active",
            value = activeTasks.toString(),
            icon = Icons.AutoMirrored.Filled.Assignment,
            color = Color(0xFF1976D2)
        )
        StatBoxSmall(
            modifier = Modifier.weight(1f),
            label = "Done",
            value = completedTasks.toString(),
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF616161)
        )
    }
}

@Composable
fun StatBoxSmall(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.height(85.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
                Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LocationServiceBanner(
    hasPermission: Boolean,
    isGpsEnabled: Boolean,
    onEnable: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationDisabled, 
                null, 
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!hasPermission) "Location Permission Required" else "GPS is Disabled", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 14.sp
                )
                Text(
                    text = "GPS is mandatory to receive new tasks near you.", 
                    fontSize = 11.sp, 
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Button(
                onClick = onEnable,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("ENABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveTaskListItem(
    task: DriverTask, 
    onClick: () -> Unit,
    vendorLocation: LatLngData? = null
) {
    val isPickupToVendor = task.isPickupToVendor
    val accentColor = if (isPickupToVendor) Color(0xFF6366F1) else Color(0xFF10B981)
    
    val pickupToDropDistance = remember(task.order, vendorLocation) {
        val pickup = task.order.pickupAddress
        val delivery = task.order.deliveryAddress
        
        if (isPickupToVendor) {
            // Distance from Customer Pickup to Vendor
            if (vendorLocation != null) {
                LocationUtils.calculateDistance(
                    pickup.latitude, pickup.longitude,
                    vendorLocation.lat, vendorLocation.lng
                )
            } else null
        } else {
            // Distance from Vendor to Customer Drop
            if (vendorLocation != null) {
                LocationUtils.calculateDistance(
                    vendorLocation.lat, vendorLocation.lng,
                    delivery.latitude, delivery.longitude
                )
            } else null
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPickupToVendor) Icons.Default.ShoppingBag else Icons.Default.LocalShipping,
                            null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isPickupToVendor) "Customer → Vendor" else "Vendor → Customer",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = accentColor
                    )
                    Text(
                        task.order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Surface(
                    color = Color(0xFFF1F3F4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "#${task.order.id.takeLast(4).uppercase()}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Route Info
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RadioButtonChecked, null, modifier = Modifier.size(14.dp), tint = accentColor)
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    val pickupText = if (isPickupToVendor) {
                        val addr = task.order.pickupAddress
                        buildString {
                            if (addr.areaLocality.isNotEmpty()) append("${addr.areaLocality}, ")
                            if (addr.city.isNotEmpty()) append(addr.city)
                            if (this.isEmpty()) append("Customer Location")
                        }.trim().removeSuffix(",")
                    } else {
                        task.order.vendorName
                    }
                    
                    val dropText = if (isPickupToVendor) {
                        task.order.vendorName
                    } else {
                        val addr = task.order.deliveryAddress
                        buildString {
                            if (addr.areaLocality.isNotEmpty()) append("${addr.areaLocality}, ")
                            if (addr.city.isNotEmpty()) append(addr.city)
                            if (this.isEmpty()) append("Customer Location")
                        }.trim().removeSuffix(",")
                    }

                    Text(
                        text = "Pickup: $pickupText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Drop: $dropText",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                pickupToDropDistance?.let { dist ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL DISTANCE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(LocationUtils.formatDistance(dist), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { getTaskProgress(task.order.status) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.1f)
            )
        }
    }
}

private fun getTaskProgress(status: OrderStatus): Float {
    return when (status) {
        OrderStatus.PICKUP_ASSIGNED, OrderStatus.DELIVERY_ASSIGNED -> 0.2f
        OrderStatus.PICKED_UP_FROM_CUSTOMER, OrderStatus.PICKED_UP_FROM_VENDOR -> 0.6f
        OrderStatus.RECEIVED_BY_VENDOR, OrderStatus.DELIVERED -> 1.0f
        else -> 0.4f
    }
}

@Composable
fun CompletedTaskListItem(task: DriverTask) {
    val isPickupToVendor = task.isPickupToVendor
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint = if (isPickupToVendor) Color(0xFF6366F1).copy(alpha = 0.6f) else Color(0xFF10B981).copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.order.customerName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (isPickupToVendor) "Customer → Vendor Completed" else "Vendor → Customer Completed",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                "#${task.order.id.takeLast(4).uppercase()}",
                fontSize = 10.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        if (count > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun OfflineBanner(onGoToProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFFCC80))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Currently Offline", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 14.sp)
                Text("Enable status in profile to receive tasks", fontSize = 11.sp, color = Color(0xFFE65100).copy(alpha = 0.8f))
            }
            Button(
                onClick = onGoToProfile,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("GO ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AvailableTaskCard(
    task: Order, 
    onAccept: () -> Unit,
    onReject: () -> Unit,
    driverLocation: LatLngData? = null,
    vendorLocation: LatLngData? = null
) {
    val isPickupToVendor = task.status == OrderStatus.ACCEPTED_BY_VENDOR
    val accentColor = if (isPickupToVendor) Color(0xFF1976D2) else Color(0xFF2E7D32)
    
    val driverToPickupDistance = remember(driverLocation, vendorLocation, isPickupToVendor) {
        if (driverLocation == null) return@remember null
        
        val targetLocation = if (isPickupToVendor) {
            // Target is Customer for Pickup
            LatLngData(task.pickupAddress.latitude, task.pickupAddress.longitude)
        } else {
            // Target is Vendor for Delivery Pickup
            vendorLocation
        }
        
        if (targetLocation != null) {
            LocationUtils.calculateDistance(
                driverLocation.lat,
                driverLocation.lng,
                targetLocation.lat,
                targetLocation.lng
            )
        } else null
    }

    val earning = if (isPickupToVendor) task.pickupFee else task.deliveryFee

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isPickupToVendor) Icons.Default.ShoppingBag else Icons.Default.LocalShipping, 
                                null, 
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isPickupToVendor) "Customer → Vendor" else "Vendor → Customer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            "ID: #${task.id.take(6).uppercase()}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (isPickupToVendor) "PICKUP" else "DELIVERY",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Route Info
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RadioButtonChecked, null, modifier = Modifier.size(12.dp), tint = accentColor)
                    Box(modifier = Modifier.width(1.dp).height(18.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    val pickupText = if (isPickupToVendor) {
                        val addr = task.pickupAddress
                        buildString {
                            if (addr.areaLocality.isNotEmpty()) append("${addr.areaLocality}, ")
                            if (addr.city.isNotEmpty()) append(addr.city)
                            if (this.isEmpty()) append("Customer Location")
                        }.trim().removeSuffix(",")
                    } else {
                        task.vendorName
                    }
                    
                    val dropText = if (isPickupToVendor) {
                        task.vendorName
                    } else {
                        val addr = task.deliveryAddress
                        buildString {
                            if (addr.areaLocality.isNotEmpty()) append("${addr.areaLocality}, ")
                            if (addr.city.isNotEmpty()) append(addr.city)
                            if (this.isEmpty()) append("Customer Location")
                        }.trim().removeSuffix(",")
                    }

                    Text(
                        text = "Pickup: $pickupText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Drop: $dropText",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp), color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DISTANCE TO PICKUP", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        if (driverToPickupDistance != null) LocationUtils.formatDistance(driverToPickupDistance) else "Calculating...", 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (isPickupToVendor) "PICKUP FEE" else "DELIVERY FEE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("₹${earning.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Reject", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Accept Task", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptySectionPlaceholder(text: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(48.dp), 
            tint = Color.LightGray.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
