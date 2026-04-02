package com.laundryapp.admin.presentation.services

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.laundryapp.core.data.model.Offer
import com.laundryapp.core.data.model.OfferType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOfferApprovalScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOfferApprovalViewModel = hiltViewModel()
) {
    val pendingOffers by viewModel.pendingOffers.collectAsState()
    val approvedOffers by viewModel.approvedOffers.collectAsState()
    val rejectedOffers by viewModel.rejectedOffers.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    
    val context = LocalContext.current
    var showCreateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Approved", "Rejected")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Offer Management", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreateSheet = true }) {
                            Icon(Icons.Default.AddCircle, "Create Global Offer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val currentList = when (selectedTab) {
                0 -> pendingOffers
                1 -> approvedOffers
                else -> rejectedOffers
            }

            if (currentList.isEmpty()) {
                EmptyOffersPlaceholder(tabs[selectedTab])
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentList, key = { it.offer.id }) { offerWithVendor ->
                        OfferStatusCard(
                            offerWithVendor = offerWithVendor,
                            status = when (selectedTab) {
                                0 -> OfferStatus.PENDING
                                1 -> OfferStatus.APPROVED
                                else -> OfferStatus.REJECTED
                            },
                            onApprove = { viewModel.approveOffer(offerWithVendor.offer.id) },
                            onReject = { viewModel.rejectOffer(offerWithVendor.offer.id) },
                            onDelete = { viewModel.deleteOffer(offerWithVendor.offer.id) }
                        )
                    }
                }
            }
        }
        
        if (showCreateSheet) {
            AdminAddOfferBottomSheet(
                isGenerating = isGenerating,
                onDismiss = { showCreateSheet = false },
                onGenerateAI = { t, c, v, ty, m, onGen -> 
                    viewModel.generateDescription(t, c, v, ty, m, onGen)
                },
                onConfirm = { 
                    viewModel.createAdminOffer(it)
                    showCreateSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddOfferBottomSheet(
    isGenerating: Boolean,
    onDismiss: () -> Unit, 
    onGenerateAI: (String, String, String, OfferType, String, (String) -> Unit) -> Unit,
    onConfirm: (Offer) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var minOrder by remember { mutableStateOf("") }
    var maxDiscountString by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(OfferType.PERCENTAGE) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Create Global Admin Offer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            
            Text(
                "This offer will be visible to all vendors and customers instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Offer Title") },
                placeholder = { Text("e.g. Grand Opening") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    if (input.length <= 10 && input.all { it.isLetterOrDigit() }) {
                        code = input.uppercase()
                    }
                },
                label = { Text("Promo Code") },
                placeholder = { Text("e.g. WELCOME100") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                prefix = { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(18.dp)) },
                supportingText = { Text("${code.length}/10 Characters (Alphanumeric only, min 7)") },
                isError = code.isNotEmpty() && code.length < 7
            )

            Column {
                Text(
                    "Discount Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TypeOption(
                        label = "Percentage",
                        icon = Icons.Default.Percent,
                        selected = type == OfferType.PERCENTAGE,
                        onClick = { type = OfferType.PERCENTAGE },
                        modifier = Modifier.weight(1f)
                    )
                    TypeOption(
                        label = "Flat Amount",
                        icon = Icons.Default.Payments,
                        selected = type == OfferType.FLAT,
                        onClick = { type = OfferType.FLAT },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.all { c -> c.isDigit() }) value = it },
                    label = { Text(if (type == OfferType.FLAT) "Discount (₹)" else "Discount (%)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minOrder,
                    onValueChange = { if (it.all { c -> c.isDigit() }) minOrder = it },
                    label = { Text("Min Order (₹)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            if (type == OfferType.PERCENTAGE) {
                OutlinedTextField(
                    value = maxDiscountString,
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxDiscountString = it },
                    label = { Text("Max Discount Upto (₹)") },
                    placeholder = { Text("e.g. 200") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text("Capped amount for percentage discount") }
                )
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Offer Description",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    
                    if (title.isNotEmpty() && code.length >= 7 && value.isNotEmpty()) {
                        TextButton(
                            onClick = { onGenerateAI(title, code, value, type, minOrder) { description = it } },
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Generate", fontSize = 12.sp)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Explain the benefits of this offer...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (title.isNotEmpty() && code.length >= 7 && value.isNotEmpty()) {
                        onConfirm(Offer(
                            title = title,
                            description = description,
                            code = code.uppercase(),
                            value = value.toDoubleOrNull() ?: 0.0,
                            minOrderAmount = minOrder.toDoubleOrNull() ?: 0.0,
                            maxDiscount = if (type == OfferType.PERCENTAGE) maxDiscountString.toDoubleOrNull() else null,
                            type = type,
                            approved = true,
                            active = true
                        ))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = title.isNotEmpty() && code.length >= 7 && value.isNotEmpty()
            ) {
                Text("CREATE GLOBAL OFFER", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun TypeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(18.dp), 
                tint = if (selected) Color.White else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else Color.DarkGray,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyOffersPlaceholder(status: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocalOffer,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No $status offers found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

enum class OfferStatus {
    PENDING, APPROVED, REJECTED
}

@Composable
fun OfferStatusCard(
    offerWithVendor: OfferWithVendor,
    status: OfferStatus,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    val offer = offerWithVendor.offer
    val isAdminOffer = offer.vendorId.isEmpty()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Offer") },
            text = { Text("Are you sure you want to permanently delete this global offer?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isAdminOffer) Icons.Default.AdminPanelSettings else Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = offerWithVendor.vendorName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                val (statusText, statusColor, statusBg) = when (status) {
                    OfferStatus.PENDING -> Triple("PENDING", Color(0xFFFFA000), Color(0xFFFFF8E1))
                    OfferStatus.APPROVED -> Triple("APPROVED", Color(0xFF4CAF50), Color(0xFFE8F5E9))
                    OfferStatus.REJECTED -> Triple("REJECTED", Color(0xFFF44336), Color(0xFFFFEBEE))
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            if (offer.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val discountText = if (offer.type == OfferType.FLAT) "₹${offer.value.toInt()} OFF" else "${offer.value.toInt()}% OFF"
                    Text(
                        text = discountText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFE91E63),
                        fontWeight = FontWeight.ExtraBold
                    )
                    val maxDiscount = offer.maxDiscount
                    if (offer.type == OfferType.PERCENTAGE && maxDiscount != null) {
                        Text(
                            text = "Upto ₹${maxDiscount.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Min. Order: ₹${offer.minOrderAmount.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PROMO CODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = offer.code,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (status != OfferStatus.REJECTED) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (status == OfferStatus.APPROVED) "Revoke" else "Reject", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (status != OfferStatus.APPROVED) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (status == OfferStatus.REJECTED) "Approve" else "Approve", fontWeight = FontWeight.Bold)
                    }
                }

                if (isAdminOffer) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Global Offer",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
