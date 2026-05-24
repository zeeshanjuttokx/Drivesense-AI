package com.example.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DriveSenseViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.example.ui.theme.HDEmerald
import com.example.ui.theme.HDAmber
import com.example.ui.theme.HDCritical
import com.google.maps.android.compose.*

import androidx.compose.ui.platform.LocalContext
import com.example.R

@Composable
fun MapScreen(viewModel: DriveSenseViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.sensorState.collectAsState()
    val fleet by viewModel.fleetData.collectAsState()
    val context = LocalContext.current
    
    // SF Area center
    val initialPos = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 13f)
    }

    var selectedVehicleInfo by remember { mutableStateOf<String?>(null) }
    var showMap by remember { mutableStateOf(false) }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        )
    }

    // This is to avoid a crash if API key is invalid immediately, though Maps SDK handles it somewhat gracefully.
    LaunchedEffect(Unit) { showMap = true }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FLEET TRACKING & GPS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("ACTIVE FLEET: ${fleet.size + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (showMap) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
                    properties = mapProperties
                ) {
                    // Main Vehicle
                    val mainMarkerState = rememberMarkerState(position = LatLng(state.latitude, state.longitude))
                    mainMarkerState.position = LatLng(state.latitude, state.longitude)
                    MarkerInfoWindow(
                        state = mainMarkerState,
                        title = state.vehicleName,
                        snippet = "Spd: ${state.speed} km/h | Health: ${state.overallHealthScore}",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE),
                        onClick = {
                            selectedVehicleInfo = "${state.vehicleId} (Main) - Speed: ${state.speed} km/h\nHealth: ${state.overallHealthScore}/100"
                            false 
                        }
                    )

                    // Fleet Vehicles
                    fleet.forEach { v ->
                        val hue = if (v.healthScore > 80) com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                                  else if (v.healthScore > 50) com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                                  else com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED

                        val vMarkerState = rememberMarkerState(key = v.id, position = LatLng(v.latitude, v.longitude))
                        vMarkerState.position = LatLng(v.latitude, v.longitude)
                        MarkerInfoWindow(
                            state = vMarkerState,
                            title = v.name,
                            snippet = "Driver: ${v.driverName} | Spd: ${v.speed}",
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue),
                            onClick = {
                                selectedVehicleInfo = "${v.id} - ${v.driverName}\nSpeed: ${v.speed} km/h | Health: ${v.healthScore}/100"
                                false 
                            }
                        )
                    }
                }
            }
            
            // Analytics Overlay
            if (selectedVehicleInfo != null) {
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(selectedVehicleInfo ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
