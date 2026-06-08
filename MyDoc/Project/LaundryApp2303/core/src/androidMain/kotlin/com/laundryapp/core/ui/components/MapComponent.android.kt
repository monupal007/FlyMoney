package com.laundryapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng as AndroidLatLng
import com.google.maps.android.compose.*
import com.laundryapp.core.util.LatLng

@Composable
actual fun MapComponent(
    modifier: Modifier,
    initialLocation: LatLng,
    onMapClick: (LatLng) -> Unit,
    showUserLocation: Boolean
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            AndroidLatLng(initialLocation.latitude, initialLocation.longitude), 
            15f
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { 
            onMapClick(LatLng(it.latitude, it.longitude))
        },
        properties = MapProperties(isMyLocationEnabled = showUserLocation)
    ) {
        Marker(
            state = MarkerState(position = AndroidLatLng(initialLocation.latitude, initialLocation.longitude)),
            title = "Selected Location"
        )
    }
}
