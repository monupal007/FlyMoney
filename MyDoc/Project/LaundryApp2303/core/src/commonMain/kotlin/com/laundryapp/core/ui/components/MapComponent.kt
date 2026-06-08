package com.laundryapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.laundryapp.core.util.LatLng

@Composable
expect fun MapComponent(
    modifier: Modifier,
    initialLocation: LatLng,
    onMapClick: (LatLng) -> Unit,
    showUserLocation: Boolean
)
