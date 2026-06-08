package com.laundryapp.admin.presentation.vendors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDetailScreen(
    vendorId: String,
    onNavigateBack: () -> Unit,
    viewModel: VendorDetailViewModel = koinViewModel()
) {
    val vendor by viewModel.vendor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.loadVendor(vendorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && vendor == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && vendor == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadVendor(vendorId) }) {
                        Text("Retry")
                    }
                }
            } else if (vendor == null) {
                Text("Vendor not found", modifier = Modifier.align(Alignment.Center))
            } else {
                val v = vendor!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Basic Info Card
                    InfoSection(title = "Business Information") {
                        DetailItem(label = "Shop Name", value = v.shopName, icon = Icons.Default.Store)
                        DetailItem(label = "Owner Name", value = v.ownerName, icon = Icons.Default.Person)
                        DetailItem(label = "Email", value = v.email, icon = Icons.Default.Email)
                        DetailItem(label = "Phone", value = v.phone, icon = Icons.Default.Phone)
                        DetailItem(label = "Address", value = v.address, icon = Icons.Default.LocationOn)
                        DetailItem(label = "Services", value = v.services.joinToString(", "), icon = Icons.Default.Handyman)
                    }

                    // Documents Section
                    InfoSection(title = "Documents") {
                        DocumentItem(label = "Aadhaar Card", imageUrl = v.documents.aadhaarUrl)
                        Spacer(modifier = Modifier.height(12.dp))
                        DocumentItem(label = "PAN Card", imageUrl = v.documents.panUrl)
                        if (v.documents.shopLicenseUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            DocumentItem(label = "Shop License", imageUrl = v.documents.shopLicenseUrl)
                        }
                    }

                    // Action Buttons
                    if (!v.isApproved && !v.isRejected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.rejectVendor(v.vendorId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                Text("Reject")
                            }
                            Button(
                                onClick = { viewModel.approveVendor(v.vendorId) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                Text("Approve")
                            }
                        }
                    } else {
                        val statusColor = if (v.isApproved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        val statusText = if (v.isApproved) "Vendor Approved" else "Vendor Rejected"
                        val statusIcon = if (v.isApproved) Icons.Default.CheckCircle else Icons.Default.Cancel

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(statusIcon, null, tint = statusColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value.ifEmpty { "N/A" }, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DocumentItem(label: String, imageUrl: String) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("No image uploaded", color = Color.Gray)
            }
        }
    }
}
