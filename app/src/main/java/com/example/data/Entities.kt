package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val severity: Severity,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val component: String,
    val failureProbability: Float, // 0.0 to 1.0
    val estimatedDaysRemaining: Int,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Severity {
    INFO, WARNING, CRITICAL
}

data class DiagnosticCode(
    val code: String,
    val system: String,
    val description: String,
    val severity: Severity
)

data class FleetVehicle(
    val id: String,
    val name: String,
    val driverName: String,
    val latitude: Double,
    val longitude: Double,
    val healthScore: Int,
    val speed: Int,
    val isEmergency: Boolean = false
)

// Live Sensor state (not in Room)
data class SensorState(
    val vehicleId: String = "8829-X",
    val vehicleName: String = "MODEL S PLATINUM",
    val driverName: String = "Alex Rivera",
    val engineTemp: Float = 90f, // Celsius
    val batteryHealth: Float = 100f, // Percent
    val tirePressureFL: Float = 32f, // PSI
    val tirePressureFR: Float = 32f,
    val tirePressureRL: Float = 32f,
    val tirePressureRR: Float = 32f,
    val fuelLevel: Float = 80f, // Percent
    val oilPressure: Float = 40f, // PSI
    val rpm: Int = 0,
    val speed: Int = 0, // km/h
    val engineVibration: Float = 0.5f, // g-force
    val driverSafetyScore: Int = 94, // 0-100 score based on behavior
    val driverEfficiencyScore: Int = 88,
    val activeDtcs: List<DiagnosticCode> = emptyList(),
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194
) {
    val overallHealthScore: Int
        get() {
            var score = 100
            if (engineTemp > 105) score -= 20 else if (engineTemp > 100) score -= 10
            if (batteryHealth < 80) score -= 10 else if (batteryHealth < 50) score -= 20
            val avgTire = (tirePressureFL + tirePressureFR + tirePressureRL + tirePressureRR) / 4
            if (avgTire < 28) score -= 15 else if (avgTire < 30) score -= 5
            if (fuelLevel < 10) score -= 5
            if (oilPressure < 20) score -= 20
            if (engineVibration > 2.0f) score -= 15
            if (driverSafetyScore < 70) score -= 10 // Safety impacts overall system health
            return score.coerceIn(0, 100)
        }
}
