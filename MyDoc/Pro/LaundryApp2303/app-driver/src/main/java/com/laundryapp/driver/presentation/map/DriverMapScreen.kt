package com.laundryapp.driver.presentation.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.laundryapp.core.data.model.Order
import com.laundryapp.core.ui.components.GpsDialog
import com.laundryapp.core.ui.components.LocationPermissionRationaleDialog
import com.laundryapp.driver.R
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DriverMapScreen(
    onNavigateBack: () -> Unit,
    onTaskClick: (String) -> Unit,
    viewModel: DriverMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val driverLocation by viewModel.driverLocation.collectAsState()
    val nearbyTasks by viewModel.nearbyTasks.collectAsState()

    // GPS and Permission States
    var showGpsDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchCurrentLocation(context)
        }
    }

    // Initialize MapView
    val mapView = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(14.0)
        }
    }

    // Handle Lifecycle for OSMdroid
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    fun triggerSystemGpsSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        client.checkLocationSettings(builder.build()).addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    settingResultRequest.launch(intentSenderRequest)
                } catch (_: IntentSender.SendIntentException) { }
            }
        }
    }

    fun checkGpsAndResolve() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            viewModel.fetchCurrentLocation(context)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                showGpsDialog = true
            }
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            checkGpsAndResolve()
        }
    }

    // Update overlays and center when data changes
    LaunchedEffect(nearbyTasks, driverLocation, locationPermissionState.status.isGranted) {
        mapView.overlays.clear()
        
        // Add user location overlay
        if (locationPermissionState.status.isGranted) {
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            myLocationOverlay.enableMyLocation()
            mapView.overlays.add(myLocationOverlay)
        }

        // Add 5km Circle around driver
        driverLocation?.let { loc ->
            val center = GeoPoint(loc.lat, loc.lng)
            mapView.controller.animateTo(center)
            
            val circlePoints = Polygon.pointsAsCircle(center, 5000.0)
            val circle = Polygon(mapView)
            circle.points = circlePoints
            circle.fillPaint.color = android.graphics.Color.argb(30, 0, 100, 255)
            circle.outlinePaint.color = android.graphics.Color.argb(100, 0, 100, 255)
            circle.outlinePaint.strokeWidth = 2f
            mapView.overlays.add(circle)
        }

        // Add Task Markers - ONLY if mapView is not detached/null
        nearbyTasks.forEach { task ->
            try {
                val marker = Marker(mapView)
                marker.position = GeoPoint(task.pickupAddress.latitude, task.pickupAddress.longitude)
                marker.title = "Pickup: ${task.customerName}"
                marker.subDescription = String.format(Locale.getDefault(), "₹%.0f | %.1f km", task.pickupFee, task.distance)
                
                // Set Custom Icon
                marker.icon = getTaskMarkerIcon(task, context)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                marker.setOnMarkerClickListener { _, _ ->
                    onTaskClick(task.id)
                    true
                }
                mapView.overlays.add(marker)
            } catch (e: Exception) {
                // Log or handle the case where Marker creation fails
                e.printStackTrace()
            }
        }
        mapView.invalidate()
    }

    if (showPermissionRationale) {
        LocationPermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false },
            onContinue = {
                showPermissionRationale = false
                locationPermissionState.launchPermissionRequest()
            }
        )
    }

    if (showGpsDialog) {
        GpsDialog(
            onDismiss = { showGpsDialog = false },
            onEnable = {
                showGpsDialog = false
                triggerSystemGpsSettings()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationPermissionState.status.isGranted) {
                        driverLocation?.let {
                            mapView.controller.animateTo(GeoPoint(it.lat, it.lng))
                        } ?: run {
                            checkGpsAndResolve()
                        }
                    } else {
                        if (locationPermissionState.status.shouldShowRationale) {
                            showPermissionRationale = true
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (locationPermissionState.status.isGranted) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay for Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Found ${nearbyTasks.size} tasks in your 5km area",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Location permission is required to see nearby tasks.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (locationPermissionState.status.shouldShowRationale) {
                                showPermissionRationale = true
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

private fun getTaskMarkerIcon(order: Order, context: Context): Drawable? {
    val resId = R.drawable.ic_laundry
    return ContextCompat.getDrawable(context, resId)
}
