package com.example.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiagnosticCode
import com.example.data.Severity
import com.example.ui.theme.HDAmber
import com.example.ui.theme.HDCritical
import com.example.ui.theme.HDEmerald
import com.example.viewmodel.DriveSenseViewModel

@Composable
fun DiagnosticsScreen(viewModel: DriveSenseViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.sensorState.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("OBD-II DIAGNOSTIC SCANNER", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        if (state.activeDtcs.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp).background(HDEmerald.copy(alpha = 0.1f), RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = HDEmerald, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No Diagnostic Trouble Codes (DTC) detected.", color = HDEmerald, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.activeDtcs) { dtc ->
                    DtcItem(dtc, viewModel)
                }
            }
        }
    }
}

@Composable
fun DtcItem(dtc: DiagnosticCode, viewModel: DriveSenseViewModel) {
    val color = when(dtc.severity) {
        Severity.CRITICAL -> HDCritical
        Severity.WARNING -> HDAmber
        Severity.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(dtc.code, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                }
                Text(dtc.system.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = color.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(16.dp))
            Text(dtc.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.sendAssistantMessage("Explain OBD-II code ${dtc.code}: ${dtc.description} and what I should do about it.") },
                colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("AI DIAGNOSE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
