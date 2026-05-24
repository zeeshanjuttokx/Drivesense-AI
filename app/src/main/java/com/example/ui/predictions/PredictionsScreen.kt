package com.example.ui.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Prediction
import com.example.ui.theme.HDCritical
import com.example.ui.theme.HDEmerald
import com.example.ui.theme.HDAmber
import com.example.viewmodel.DriveSenseViewModel

@Composable
fun PredictionsScreen(viewModel: DriveSenseViewModel, modifier: Modifier = Modifier) {
    val predictions by viewModel.predictions.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("AI PREDICTIVE MAINTENANCE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        if (predictions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("System healthy. No predicted failures.", color = HDEmerald)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(predictions) { prediction ->
                    PredictionItem(prediction)
                }
            }
        }
    }
}

@Composable
fun PredictionItem(prediction: Prediction) {
    val color = when {
        prediction.failureProbability > 0.8f -> HDCritical
        prediction.failureProbability > 0.5f -> HDAmber
        else -> HDEmerald
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(prediction.component.uppercase(), style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("RISK: ${(prediction.failureProbability * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = color)
            }
            
            Box(Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))) {
                Box(Modifier.fillMaxWidth(prediction.failureProbability).fillMaxHeight().background(color, RoundedCornerShape(4.dp)))
            }
            
            Column {
                Text(if (prediction.estimatedDaysRemaining <= 1) "CRITICAL: Imminent Failure" else "EST. FAILURE IN: ${prediction.estimatedDaysRemaining} DAYS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = if (prediction.estimatedDaysRemaining <= 1) HDCritical else color)
                Spacer(Modifier.height(6.dp))
                Text(prediction.recommendation, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
