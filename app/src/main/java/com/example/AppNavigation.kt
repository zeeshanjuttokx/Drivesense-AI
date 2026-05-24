package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.diagnostics.DiagnosticsScreen
import com.example.ui.map.MapScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.predictions.PredictionsScreen
import com.example.viewmodel.DriveSenseViewModel

@Composable
fun AppNavigation(viewModel: DriveSenseViewModel = viewModel()) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    var showAssistant by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAssistant = true },
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.Info, contentDescription = "AI Assistant")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = { currentRoute = "dashboard" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Dashboard") },
                    label = { Text("App") }
                )
                NavigationBarItem(
                    selected = currentRoute == "diagnostics",
                    onClick = { currentRoute = "diagnostics" },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Diagnostics") },
                    label = { Text("OBD-II") }
                )
                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = { currentRoute = "map" },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = currentRoute == "predictions",
                    onClick = { currentRoute = "predictions" },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "AI") },
                    label = { Text("AI") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { currentRoute = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Simulate") },
                    label = { Text("Simulate") }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentRoute) {
            "dashboard" -> DashboardScreen(viewModel, modifier)
            "diagnostics" -> DiagnosticsScreen(viewModel, modifier)
            "map" -> MapScreen(viewModel, modifier)
            "predictions" -> PredictionsScreen(viewModel, modifier)
            "settings" -> SettingsScreen(viewModel, modifier)
        }

        
        if (showAssistant) {
            com.example.ui.assistant.AssistantBottomSheet(
                viewModel = viewModel,
                onDismissRequest = { showAssistant = false }
            )
        }
    }
}

@Composable
fun SettingsScreen(viewModel: DriveSenseViewModel, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        androidx.compose.material3.Text("SIMULATION CONTROLS", style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
        androidx.compose.material3.Button(onClick = { viewModel.triggerEvent("overheat") }) { Text("Simulate Overheating") }
        androidx.compose.material3.Button(onClick = { viewModel.triggerEvent("tire") }) { Text("Simulate Tire Leak") }
        androidx.compose.material3.Button(onClick = { viewModel.triggerEvent("battery") }) { Text("Simulate Battery Drain") }
        androidx.compose.material3.Button(onClick = { viewModel.triggerEvent("brake") }) { Text("Simulate Harsh Braking Incident") }
        androidx.compose.foundation.layout.Spacer(Modifier.padding(16.dp))
        androidx.compose.material3.Button(onClick = { viewModel.triggerEvent("reset") }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error)) { Text("Reset Fleet Simulation") }
    }
}
