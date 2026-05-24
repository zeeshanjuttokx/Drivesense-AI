package com.example.simulation

import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class SimulationEngine(private val repository: DriveSenseRepository) {
    private val _sensorState = MutableStateFlow(SensorState())
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()
    
    private val _fleetData = MutableStateFlow<List<FleetVehicle>>(emptyList())
    val fleetData: StateFlow<List<FleetVehicle>> = _fleetData.asStateFlow()
    
    // External override flags to force issues
    var forceOverheat = false
    var forceTireDrop = false
    var forceBatteryDrain = false
    var forceHarshBrake = false

    init {
        _fleetData.value = listOf(
            FleetVehicle("V-101", "Delivery Truck A", "Sarah Jenkins", 37.7740, -122.4200, 95, 45, false),
            FleetVehicle("V-102", "Sprinter Van 2", "Mike Ross", 37.7755, -122.4180, 88, 30, false),
            FleetVehicle("V-103", "EV Transporter", "Lisa Wong", 37.7730, -122.4190, 70, 0, false),
            FleetVehicle("V-104", "Heavy Hauler", "David Kim", 37.7760, -122.4220, 45, 60, true)
        )
    }

    suspend fun startSimulation() {
        while (true) {
            delay(1500)
            updateSensors()
            updateFleet()
            analyzeDataAndPredict()
        }
    }

    private fun updateFleet() {
        _fleetData.update { fleet ->
            fleet.map { v ->
                val isMoving = Random.nextFloat() > 0.3f
                val newSpeed = if (isMoving) {
                    (v.speed + Random.nextInt(-5, 6)).coerceIn(0, 80)
                } else 0
                val moveLat = if(isMoving) (Random.nextDouble() * 0.0004) - 0.0002 else 0.0
                val moveLng = if(isMoving) (Random.nextDouble() * 0.0004) - 0.0002 else 0.0
                v.copy(
                    latitude = v.latitude + moveLat,
                    longitude = v.longitude + moveLng,
                    speed = newSpeed
                )
            }
        }
    }

    private fun updateSensors() {
        _sensorState.update { currentState ->
            val isDriving = currentState.rpm > 0 || Random.nextFloat() > 0.7f
            var driveFactor = if (isDriving) 1 else 0

            var newSpeed = if(isDriving) Random.nextInt(20, 80) else 0
            
            // Driver physics
            var newSafety = currentState.driverSafetyScore
            var newEff = currentState.driverEfficiencyScore
            
            if (forceHarshBrake) {
                newSpeed = (currentState.speed - 40).coerceAtLeast(0) // sudden drop
                newSafety = (newSafety - 5).coerceAtLeast(0)
                newEff = (newEff - 2).coerceAtLeast(0)
            } else if (isDriving && Random.nextFloat() > 0.95f) { // random aggressive acceleration
                newSafety = (newSafety - 1).coerceAtLeast(0)
                newEff = (newEff - 1).coerceAtLeast(0)
            }

            val newEngineTemp = if (forceOverheat) {
                (currentState.engineTemp + 2f).coerceAtMost(115f)
            } else if (isDriving) {
                (currentState.engineTemp + Random.nextFloat() * 2 - 0.7f).coerceIn(60f, 100f)
            } else {
                (currentState.engineTemp - Random.nextFloat() * 2).coerceAtLeast(20f)
            }

            val newBattery = if(forceBatteryDrain) {
                (currentState.batteryHealth - 1f).coerceAtLeast(0f)
            } else {
                currentState.batteryHealth - (Random.nextFloat() * 0.01f).coerceAtLeast(0f)
            }

            val fluctuateTire = { press: Float -> 
                val base = (press + Random.nextFloat() * 0.2f - 0.1f).coerceIn(10f, 40f) 
                if (forceTireDrop) base - 0.5f else base
            }

            val newFuel = (currentState.fuelLevel - (if(isDriving) 0.05f else 0f)).coerceAtLeast(0f)
            val newRpm = if(isDriving) Random.nextInt(1500, 3500) else 0
            val baseVibration = if(isDriving) 0.4f + (newRpm / 4500f) else 0.1f
            val newVibe = baseVibration + (if(forceHarshBrake) 1.5f else Random.nextFloat() * 0.2f)

            val newOil = if(isDriving) 40f + (newRpm / 1000f) + Random.nextFloat() * 2 else 10f
            
            val dtcs = mutableListOf<DiagnosticCode>()
            if (forceOverheat) dtcs.add(DiagnosticCode("P0217", "Powertrain", "Engine Coolant Over Temperature Condition", Severity.CRITICAL))
            if (forceHarshBrake) dtcs.add(DiagnosticCode("U0415", "Network", "Invalid Data Received from ABS Control Module", Severity.WARNING))
            if (forceBatteryDrain) dtcs.add(DiagnosticCode("P0A7D", "Hybrid System", "Hybrid Battery Pack State of Charge Low", Severity.CRITICAL))
            
            forceHarshBrake = false // Reset after one tick

            currentState.copy(
                engineTemp = newEngineTemp,
                batteryHealth = newBattery,
                tirePressureFL = fluctuateTire(currentState.tirePressureFL),
                tirePressureFR = fluctuateTire(currentState.tirePressureFR),
                tirePressureRL = fluctuateTire(currentState.tirePressureRL),
                tirePressureRR = fluctuateTire(currentState.tirePressureRR),
                fuelLevel = newFuel,
                rpm = newRpm,
                speed = newSpeed,
                engineVibration = newVibe,
                oilPressure = newOil,
                driverSafetyScore = newSafety,
                driverEfficiencyScore = newEff,
                activeDtcs = dtcs,
                latitude = currentState.latitude + (if(isDriving) (Random.nextDouble() * 0.0001) - 0.00005 else 0.0),
                longitude = currentState.longitude + (if(isDriving) (Random.nextDouble() * 0.0001) - 0.00005 else 0.0)
            )
        }
    }

    private suspend fun analyzeDataAndPredict() {
        val state = sensorState.value
        
        if (state.engineTemp > 105f) {
            emitAlert("Engine Overheating", "Engine temperature has exceeded 105°C.", Severity.CRITICAL)
            updatePrediction("Engine", 0.90f, 1, "Stop vehicle immediately and check coolant system.")
        } else if (state.engineTemp > 100f) {
            emitAlert("Engine Temperature Warning", "Engine temperature is rising abnormally.", Severity.WARNING)
            updatePrediction("Engine", 0.5f, 14, "Schedule engine diagnostic soon.")
        }
        
        if (state.batteryHealth < 50f) {
            emitAlert("Battery Critical", "Battery health has dropped below 50%.", Severity.CRITICAL)
            updatePrediction("Battery", 0.85f, 5, "Replace battery immediately to avoid sudden failure.")
        } else if (state.batteryHealth < 80f) {
            emitAlert("Battery Degrading", "Battery health is steadily decreasing.", Severity.INFO)
            updatePrediction("Battery", 0.6f, 45, "Plan for a battery replacement.")
        }

        val minTire = minOf(state.tirePressureFL, state.tirePressureFR, state.tirePressureRL, state.tirePressureRR)
        if (minTire < 20f) {
             emitAlert("Tire Pressure Critical", "Tire pressure is dangerously low ($minTire PSI).", Severity.CRITICAL)
             updatePrediction("Tires", 0.8f, 1, "Inflate tires immediately. High puncture risk.")
        } else if (minTire < 28f) {
             emitAlert("Tire Pressure Low", "One or more tires has low pressure.", Severity.WARNING)
        }

        if (state.oilPressure < 20f && state.rpm > 1000) {
            emitAlert("Low Oil Pressure", "Oil pressure is low while engine is running.", Severity.CRITICAL)
            updatePrediction("Oil System", 0.9f, 2, "Check oil levels immediately and inspect pump.")
        }
        
        if (state.driverSafetyScore < 75f) {
            emitAlert("Driver Safety Alert", "Aggressive driving patterns detected. Harsh braking and acceleration recorded.", Severity.WARNING)
            updatePrediction("Brakes", 0.7f, 30, "Schedule brake pad inspection due to harsh braking events.")
        }
    }

    private var lastAlerts = mutableMapOf<String, Long>()
    private suspend fun emitAlert(title: String, desc: String, severity: Severity) {
        val now = System.currentTimeMillis()
        if (now - (lastAlerts[title] ?: 0L) > 60000) { // Throttle 1 min
            repository.addAlert(Alert(title = title, description = desc, severity = severity))
            lastAlerts[title] = now
        }
    }

    private suspend fun updatePrediction(component: String, probability: Float, days: Int, rec: String) {
        val p = repository.getPrediction(component)
        if (p == null || kotlin.math.abs(p.failureProbability - probability) > 0.05f) {
            val toSave = p?.copy(failureProbability = probability, estimatedDaysRemaining = days, recommendation = rec, timestamp = System.currentTimeMillis())
                ?: Prediction(component = component, failureProbability = probability, estimatedDaysRemaining = days, recommendation = rec)
            repository.addPrediction(toSave)
        }
    }
}
