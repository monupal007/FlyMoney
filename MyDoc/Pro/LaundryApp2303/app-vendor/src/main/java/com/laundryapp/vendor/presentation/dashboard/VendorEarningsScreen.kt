package com.laundryapp.vendor.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

enum class EarningsFilter {
    LAST_7_DAYS, LAST_30_DAYS
}

data class EarningPoint(
    val label: String,
    val amount: Double,
    val timestamp: Long
)

enum class ActivityType {
    ORDER, PAYOUT
}

data class RecentActivity(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val type: ActivityType
)

data class EarningsUiState(
    val totalEarnings: Double = 0.0,
    val averageEarnings: Double = 0.0,
    val orderCount: Int = 0,
    val points: List<EarningPoint> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val filter: EarningsFilter = EarningsFilter.LAST_7_DAYS,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorEarningsScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    viewModel: VendorEarningsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings Report", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                EarningsOverviewHeader(uiState.totalEarnings)
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryMiniCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg. Order",
                        value = "₹${String.format(Locale.getDefault(), "%.0f", uiState.averageEarnings)}",
                        icon = Icons.Default.BarChart,
                        color = Color(0xFF6366F1)
                    )
                    SummaryMiniCard(
                        modifier = Modifier.weight(1f),
                        title = "Delivered",
                        value = "${uiState.orderCount}",
                        icon = Icons.Default.ShoppingBag,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Performance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            EarningsFilterTabs(uiState.filter) { viewModel.setFilter(it) }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else {
                                ModernBarChart(uiState.points)
                            }
                        }
                    }
                }

                RecentTransactionsSection(
                    activities = uiState.recentActivities,
                    onActivityClick = { activity ->
                        if (activity.type == ActivityType.ORDER) {
                            onOrderClick(activity.id)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EarningsOverviewHeader(total: Double) {
    val animatedTotal by animateFloatAsState(
        targetValue = total.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "TotalEarnings"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "TOTAL BALANCE",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "₹${String.format(Locale.getDefault(), "%,.2f", animatedTotal)}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun SummaryMiniCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EarningsFilterTabs(selected: EarningsFilter, onFilterSelected: (EarningsFilter) -> Unit) {
    Surface(
        color = Color(0xFFF1F3F4),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            EarningsFilter.entries.forEach { filter ->
                val isSelected = selected == filter
                val backgroundColor by animateColorAsState(if (isSelected) Color.White else Color.Transparent, label = "tab")
                val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, label = "text")
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when(filter) {
                            EarningsFilter.LAST_7_DAYS -> "Weekly"
                            EarningsFilter.LAST_30_DAYS -> "Monthly"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBarChart(points: List<EarningPoint>) {
    if (points.isEmpty()) return
    val maxAmount = points.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 100.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    val amountStyle = TextStyle(fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val chartHeight = size.height - 40.dp.toPx()
        val totalBars = points.size
        val barWidth = (size.width / (totalBars * 1.5f)).coerceIn(8.dp.toPx(), 40.dp.toPx())
        val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)

        points.forEachIndexed { index, point ->
            val barHeight = (point.amount / maxAmount * (chartHeight - 30.dp.toPx())).toFloat()
            val x = spacing + index * (barWidth + spacing)
            val y = chartHeight - barHeight

            // Draw Bar Background track
            drawRoundRect(
                color = Color(0xFFF1F3F4),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw Bar with Gradient
            if (point.amount > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }

            // Draw Date Label
            val shouldDrawLabel = when {
                totalBars <= 7 -> true
                index % 5 == 0 || index == totalBars - 1 -> true
                else -> false
            }

            if (shouldDrawLabel) {
                val textLayoutResult = textMeasurer.measure(point.label, style = labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(x + (barWidth / 2) - (textLayoutResult.size.width / 2), chartHeight + 8.dp.toPx())
                )
            }

            // Draw Amount Label
            if (point.amount > 0 && shouldDrawLabel) {
                val amountText = "₹${point.amount.toInt()}"
                val amountLayoutResult = textMeasurer.measure(amountText, style = amountStyle)
                drawText(
                    textLayoutResult = amountLayoutResult,
                    topLeft = Offset(x + (barWidth / 2) - (amountLayoutResult.size.width / 2), y - 18.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    activities: List<RecentActivity>,
    onActivityClick: (RecentActivity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (activities.isNotEmpty()) {
                TextButton(onClick = { }) { Text("View All") }
            }
        }
        
        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No recent activities", color = Color.Gray)
                }
            }
        } else {
            activities.forEach { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onActivityClick(activity) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val color = when(activity.type) {
                            ActivityType.ORDER -> Color(0xFF6366F1)
                            ActivityType.PAYOUT -> Color(0xFFF59E0B)
                        }
                        val icon = when(activity.type) {
                            ActivityType.ORDER -> Icons.Default.History
                            ActivityType.PAYOUT -> Icons.AutoMirrored.Filled.TrendingUp
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon, 
                                null, 
                                modifier = Modifier.size(24.dp), 
                                tint = color
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activity.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(activity.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = "+₹${String.format(Locale.getDefault(), "%,.0f", activity.amount)}", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, 
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}
