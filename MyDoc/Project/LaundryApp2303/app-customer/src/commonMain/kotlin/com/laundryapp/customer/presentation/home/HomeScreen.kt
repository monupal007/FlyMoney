package com.laundryapp.customer.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.laundryapp.core.data.model.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToOrderPlacement: (String?) -> Unit,
    onNavigateToOrderTracking: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    // Redirect to add address if no addresses are set
    LaunchedEffect(uiState.isLoading, uiState.hasAddresses) {
        if (!uiState.isLoading && !uiState.hasAddresses) {
            onNavigateToAddAddress()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(bottom = 50.dp)
            ) {
                // Header with Address
                item {
                    HomeHeader(
                        userName = uiState.user?.name ?: "Guest",
                        profileImageUrl = uiState.user?.profileImageUrl ?: "",
                        defaultAddress = uiState.defaultAddress,
                        onProfileClick = onNavigateToProfile,
                        onAddressClick = onNavigateToAddAddress
                    )
                }

                // Our Services Section
                item {
                    SectionHeader(title = "Our Services", onSeeAllClick = null)
                    SpacerHeight(12)
                    
                    if (uiState.services.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No services available.", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.services) { service ->
                                ServiceCircleItem(
                                    service = service,
                                    onClick = { onNavigateToOrderPlacement(service.category) }
                                )
                            }
                        }
                    }
                }

                // Promotional Banner
                item {
                    SpacerHeight(12)
                    PromoBanner(
                        onActionClick = { onNavigateToOrderPlacement(null) }
                    )
                }

                // Offers Section
                item {
                    SectionHeader(title = "Special Offers", onSeeAllClick = {})
                    SpacerHeight(12)
                    
                    if (uiState.offers.isEmpty()) {
                        EmptyStateText("No offers available right now.")
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.offers) { offer ->
                                SmallOfferCard(
                                    offer = offer,
                                    onClick = {  }
                                )
                            }
                        }
                    }
                }

                item { SpacerHeight(12) }

                // Recent Orders Section
                if (uiState.recentOrders.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recent Orders", onSeeAllClick = onNavigateToOrderHistory)

                        Column(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.recentOrders.forEach { order ->
                                OrderSummaryCard(
                                    order = order,
                                    onClick = { onNavigateToOrderTracking(order.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Modern Side Navigation Bubble
            SideBubbleMenu(
                onNavigateToAddresses =  onNavigateToAddAddress,
                onNavigateToOrders = onNavigateToOrderHistory,
                onNavigateToProfile = onNavigateToProfile,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun SideBubbleMenu(
    onNavigateToAddresses: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize().padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Dim background when expanded
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { expanded = false }
            )
        }

        Column(
            modifier = Modifier.padding(bottom = 32.dp, end = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically { it / 2 } + expandVertically(),
                exit = fadeOut() + slideOutVertically { it / 2 } + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BubbleMenuItem(
                        icon = Icons.Rounded.Person,
                        label = "Profile",
                        gradient = listOf(Color(0xFF818CF8), Color(0xFF6366F1)),
                        onClick = {
                            expanded = false
                            onNavigateToProfile()
                        }
                    )
                    BubbleMenuItem(
                        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                        label = "My Orders",
                        gradient = listOf(Color(0xFF34D399), Color(0xFF10B981)),
                        onClick = {
                            expanded = false
                            onNavigateToOrders()
                        }
                    )
                    BubbleMenuItem(
                        icon = Icons.Rounded.LocationOn,
                        label = "My Addresses",
                        gradient = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                        onClick = {
                            expanded = false
                            onNavigateToAddresses()
                        }
                    )
                }
            }

            // Main Trigger Bubble
            FloatingActionButton(
                onClick = { expanded = !expanded },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Widgets,
                    contentDescription = "Menu",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun BubbleMenuItem(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .background(Brush.linearGradient(gradient), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun HomeHeader(
    userName: String, 
    profileImageUrl: String,
    defaultAddress: Address?,
    onProfileClick: () -> Unit,
    onAddressClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${userName.split(" ").firstOrNull() ?: "there"}!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Fresh Clothes, Fresh Day!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Surface(
                    onClick = onProfileClick,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
            
            SpacerHeight(16)
            
            // Address Section
            Surface(
                onClick = onAddressClick,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val fullAddressText = buildString {
                            defaultAddress?.let {
                                if (it.flatHouseBuilding.isNotEmpty()) append("${it.flatHouseBuilding}, ")
                                if (it.areaLocality.isNotEmpty()) append("${it.areaLocality}, ")
                                append(it.fullAddress)
                            } ?: append("Add your service address")
                        }
                        
                        Text(
                            text = if (defaultAddress != null) "Delivery Address" else "Location",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fullAddressText,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
fun ServiceCircleItem(service: LaundryService, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getServiceIcon(service.name),
                    contentDescription = service.name,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        SpacerHeight(8)
        Text(
            text = service.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
fun PromoBanner(onActionClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF1565C0)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "NEW USER OFFER",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                SpacerHeight(8)
                Text(
                    text = "Get 30% OFF",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
                Text(
                    text = "on your first order",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                SpacerHeight(16)
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1565C0)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Order Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "USE CODE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "FIRST30",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallOfferCard(offer: Offer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (offer.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = offer.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.LocalOffer,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = if (offer.type == OfferType.PERCENTAGE) "${offer.value.toInt()}% OFF" else "₹${offer.value.toInt()} OFF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = offer.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Code: ${offer.code}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun OrderSummaryCard(order: Order, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order #${order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${order.items.size} items • ₹${order.totalAmount.toInt()}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyStateText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
        color = Color.Gray,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

fun getServiceIcon(serviceName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        serviceName.contains("Wash", ignoreCase = true) && serviceName.contains("Iron", ignoreCase = true) -> Icons.Default.LocalLaundryService
        serviceName.contains("Wash", ignoreCase = true) -> Icons.Default.WaterDrop
        serviceName.contains("Iron", ignoreCase = true) -> Icons.Default.Iron
        serviceName.contains("Dry", ignoreCase = true) -> Icons.Default.DryCleaning
        else -> Icons.Default.LocalLaundryService
    }
}

@Composable
fun SpacerHeight(dp: Int) {
    Spacer(modifier = Modifier.height(dp.dp))
}
