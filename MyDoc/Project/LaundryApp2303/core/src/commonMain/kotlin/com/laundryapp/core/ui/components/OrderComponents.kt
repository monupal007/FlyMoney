package com.laundryapp.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.model.OrderStatus
import com.laundryapp.core.util.DateTimeUtils

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (color, label) = when (status) {
        OrderStatus.PLACED -> Color(0xFFFFA726) to "Placed"
        OrderStatus.CONFIRMED -> Color(0xFFFFA726) to "Confirmed"
        OrderStatus.ACCEPTED_BY_VENDOR -> Color(0xFF42A5F5) to "Accepted"
        OrderStatus.REJECTED_BY_VENDOR -> Color(0xFFEF5350) to "Rejected"
        OrderStatus.PICKUP_ASSIGNED -> Color(0xFF7E57C2) to "Pickup Assigned"
        OrderStatus.PICKED_UP_FROM_CUSTOMER -> Color(0xFF5C6BC0) to "Picked Up"
        OrderStatus.PICKED_UP -> Color(0xFF5C6BC0) to "Picked Up"
        OrderStatus.RECEIVED_BY_VENDOR -> Color(0xFF26A69A) to "Received"
        OrderStatus.PROCESSING -> Color(0xFF66BB6A) to "Processing"
        OrderStatus.IN_PROGRESS -> Color(0xFF66BB6A) to "Processing"
        OrderStatus.READY_FOR_DELIVERY -> Color(0xFF43A047) to "Ready"
        OrderStatus.READY -> Color(0xFF43A047) to "Ready"
        OrderStatus.DELIVERY_ASSIGNED -> Color(0xFF26C6DA) to "Delivery Assigned"
        OrderStatus.PICKED_UP_FROM_VENDOR -> Color(0xFF00ACC1) to "Picked up from Vendor"
        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF00ACC1) to "Out for delivery"
        OrderStatus.DELIVERED -> Color(0xFF4CAF50) to "Delivered"
        OrderStatus.CANCELLED -> Color(0xFFBDBDBD) to "Cancelled"
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    amountLabel: String = "Total Amount",
    amountValue: Double? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val displayAmount = amountValue ?: order.totalAmount

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Order #${order.id.take(6).uppercase()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OrderStatusChip(status = order.status)
                    }
                    Text(
                        order.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Items", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        order.items.joinToString { it.serviceName },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 12.dp)) {
                    Text(amountLabel, fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹$displayAmount",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (actions != null) {
                Spacer(modifier = Modifier.height(12.dp))
                actions()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderCardEnhanced(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Order #${order.id.take(6).uppercase()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OrderStatusChip(status = order.status)
                    }
                    Text(
                        order.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        DateTimeUtils.formatDateTime(order.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            "Update Status",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()
                        OrderStatus.entries.filter { it != order.status }.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                leadingIcon = {
                                    val icon = when(status) {
                                        OrderStatus.PLACED -> Icons.Default.Pending
                                        OrderStatus.CONFIRMED -> Icons.Default.CheckCircle
                                        OrderStatus.PICKED_UP -> Icons.Default.ShoppingBag
                                        OrderStatus.IN_PROGRESS -> Icons.Default.Sync
                                        OrderStatus.READY -> Icons.Default.Checkroom
                                        OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.LocalShipping
                                        OrderStatus.DELIVERED -> Icons.Default.DoneAll
                                        OrderStatus.CANCELLED -> Icons.Default.Cancel
                                        else -> Icons.Default.Info
                                    }
                                    Icon(icon, null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    onUpdateStatus(status)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Items", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        order.items.joinToString { it.serviceName },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 200.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Amount", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "₹${order.totalAmount}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
