package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SensorState
import com.example.ui.theme.HDCritical
import com.example.ui.theme.HDEmerald
import com.example.ui.theme.HDAmber
import com.example.viewmodel.DriveSenseViewModel

@Composable
fun DashboardScreen(viewModel: DriveSenseViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.sensorState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Bar / Header area omitted as we let Scaffold/OS handle it, or we could add a custom header.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DriveSense ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text("${state.vehicleName} • ID: ${state.vehicleId} ▾", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("FLEET: 12 ACTIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Large Health Meter Hero
        HealthMeter(score = state.overallHealthScore)
        
        Text(
            text = "\"AI prediction: Vehicle is healthy. Driver Alex Rivera is maintaining excellent efficiency.\"",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
        )

        // Telemetry Bento Grid
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Driver Analytics Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Driver Safety", "${state.driverSafetyScore}", "/100", Modifier.weight(1f), if(state.driverSafetyScore < 70) HDCritical else MaterialTheme.colorScheme.primary, (state.driverSafetyScore / 100f).coerceIn(0f, 1f))
                MetricCard("Driver Efficiency", "${state.driverEfficiencyScore}", "/100", Modifier.weight(1f), HDEmerald, (state.driverEfficiencyScore / 100f).coerceIn(0f, 1f))
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Engine Temp", "%.1f".format(state.engineTemp), "°C", Modifier.weight(1f), if(state.engineTemp > 100) HDCritical else HDEmerald, (state.engineTemp / 150f).coerceIn(0f, 1f))
                MetricCard("Battery", "%.1f".format(state.batteryHealth), "V", Modifier.weight(1f), if(state.batteryHealth < 50) HDCritical else MaterialTheme.colorScheme.primary, (state.batteryHealth / 100f).coerceIn(0f, 1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tires Card
                Card(
                    modifier = Modifier.weight(1f).height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tire Pressure".uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TireItem("FL", state.tirePressureFL)
                            TireItem("FR", state.tirePressureFR)
                            TireItem("RL", state.tirePressureRL)
                            TireItem("RR", state.tirePressureRR)
                        }
                    }
                }
                
                // Oil Pressure Card
                MetricCard("Oil Level", "%.0f".format(state.oilPressure), "%", Modifier.weight(1f), if(state.oilPressure < 20) HDCritical else HDEmerald, (state.oilPressure / 100f).coerceIn(0f, 1f))
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun TireItem(label: String, value: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("%.0f".format(value), fontWeight = FontWeight.Bold, color = if(value < 28) HDAmber else MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HealthMeter(score: Int) {
    val color = when {
        score >= 80 -> MaterialTheme.colorScheme.primary
        score >= 50 -> HDAmber
        else -> HDCritical
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF16171A),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = (score / 100f) * 270f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", fontSize = 56.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
            Text("HEALTH SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface, progress: Float = 0f) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.width(4.dp))
                    Text(unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(2.dp))) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(valueColor, RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}
