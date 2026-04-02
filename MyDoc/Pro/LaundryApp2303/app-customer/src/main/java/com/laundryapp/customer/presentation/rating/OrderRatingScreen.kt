package com.laundryapp.customer.presentation.rating

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderRatingViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrderById(orderId).collect { order ->
                _order.value = order
            }
        }
    }

    fun submitRating(
        vendorRating: Float?,
        vendorReview: String?,
        pickupDriverRating: Float?,
        pickupDriverReview: String?,
        deliveryDriverRating: Float?,
        deliveryDriverReview: String?
    ) {
        val currentOrder = _order.value ?: return
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            try {
                val result = orderRepository.submitRating(
                    orderId = currentOrder.id,
                    vendorRating = vendorRating,
                    vendorReview = vendorReview,
                    pickupDriverRating = pickupDriverRating,
                    pickupDriverReview = pickupDriverReview,
                    deliveryDriverRating = deliveryDriverRating,
                    deliveryDriverReview = deliveryDriverReview
                )
                
                if (result.isSuccess) {
                    _isSubmitted.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to submit rating"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isSubmitting.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRatingScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderRatingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val order by viewModel.order.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isSubmitted by viewModel.isSubmitted.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    LaunchedEffect(isSubmitted) {
        if (isSubmitted) {
            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rate Experience", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isSubmitting) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        order?.let { ord ->
            var vendorRating by remember { mutableFloatStateOf(0f) }
            var vendorReview by remember { mutableStateOf("") }
            var pickupRating by remember { mutableFloatStateOf(0f) }
            var pickupReview by remember { mutableStateOf("") }
            var deliveryRating by remember { mutableFloatStateOf(0f) }
            var deliveryReview by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "How was your order?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Order #${ord.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Vendor Rating Section
                ModernRatingCard(
                    title = "Laundry Quality",
                    subtitle = ord.vendorName,
                    icon = Icons.Default.LocalLaundryService,
                    rating = vendorRating,
                    onRatingChanged = { if (!isSubmitting) vendorRating = it },
                    review = vendorReview,
                    onReviewChanged = { if (!isSubmitting) vendorReview = it },
                    placeholder = "How was the cleaning quality?"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pickup Driver Rating Section
                if (ord.pickupDriverName.isNotEmpty()) {
                    ModernRatingCard(
                        title = "Pickup Driver Experience",
                        subtitle = ord.pickupDriverName,
                        icon = Icons.Default.ShoppingBag,
                        rating = pickupRating,
                        onRatingChanged = { if (!isSubmitting) pickupRating = it },
                        review = pickupReview,
                        onReviewChanged = { if (!isSubmitting) pickupReview = it },
                        placeholder = "How was the pickup service?"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Delivery Driver Rating Section
                val deliveryDriverName = ord.deliveryDriverName.ifEmpty { ord.assignedDriverName }
                if (deliveryDriverName.isNotEmpty() && deliveryDriverName != ord.pickupDriverName) {
                    ModernRatingCard(
                        title = "Delivery Experience",
                        subtitle = deliveryDriverName,
                        icon = Icons.Default.LocalShipping,
                        rating = deliveryRating,
                        onRatingChanged = { if (!isSubmitting) deliveryRating = it },
                        review = deliveryReview,
                        onReviewChanged = { if (!isSubmitting) deliveryReview = it },
                        placeholder = "How was the delivery service?"
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                } else if (deliveryDriverName.isNotEmpty() && deliveryDriverName == ord.pickupDriverName) {
                    Text(
                        "Same partner for pickup & delivery",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ModernRatingCard(
                        title = "Overall Delivery Partner",
                        subtitle = deliveryDriverName,
                        icon = Icons.Default.TwoWheeler,
                        rating = deliveryRating,
                        onRatingChanged = { if (!isSubmitting) deliveryRating = it },
                        review = deliveryReview,
                        onReviewChanged = { if (!isSubmitting) deliveryReview = it },
                        placeholder = "How was the partner's service?"
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }

                Button(
                    onClick = {
                        viewModel.submitRating(
                            vendorRating = if (vendorRating > 0) vendorRating else null,
                            vendorReview = vendorReview.takeIf { it.isNotBlank() },
                            pickupDriverRating = if (pickupRating > 0) pickupRating else null,
                            pickupDriverReview = pickupReview.takeIf { it.isNotBlank() },
                            deliveryDriverRating = if (deliveryRating > 0) deliveryRating else null,
                            deliveryDriverReview = deliveryReview.takeIf { it.isNotBlank() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting && (vendorRating > 0 || pickupRating > 0 || deliveryRating > 0),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit Review", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ModernRatingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    review: String,
    onReviewChanged: (String) -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(subtitle, color = Color.Gray, fontSize = 13.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ModernRatingBar(rating = rating, onRatingChanged = onRatingChanged)

            val ratingText = when (rating.toInt()) {
                1 -> "Very Poor"
                2 -> "Poor"
                3 -> "Good"
                4 -> "Very Good"
                5 -> "Excellent!"
                else -> "Tap to rate"
            }
            
            Text(
                text = ratingText,
                style = MaterialTheme.typography.labelLarge,
                color = if (rating > 0) MaterialTheme.colorScheme.primary else Color.LightGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = review,
                onValueChange = onReviewChanged,
                placeholder = { Text(placeholder, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ModernRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..5) {
            val isSelected = i <= rating
            val starScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "starScale"
            )
            val starColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                label = "starColor"
            )

            IconButton(
                onClick = { onRatingChanged(i.toFloat()) },
                modifier = Modifier.scale(starScale)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
