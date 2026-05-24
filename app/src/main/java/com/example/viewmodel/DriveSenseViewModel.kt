package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DriveSenseRepository
import com.example.simulation.SimulationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.ChatMessage
import com.example.data.RetrofitClient
import com.example.data.GenerateContentRequest
import com.example.data.Content
import com.example.data.Part
import com.example.BuildConfig

class DriveSenseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = DriveSenseRepository(database.driveSenseDao())
    val simulationEngine = SimulationEngine(repository)

    val sensorState = simulationEngine.sensorState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), simulationEngine.sensorState.value)
    
    val fleetData = simulationEngine.fleetData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val predictions = repository.predictions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Hello! I am your AI Vehicle Assistant. How can I help you today?", isUser = false))
    )
    val chatHistory = _chatHistory.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking = _isThinking.asStateFlow()

    fun sendAssistantMessage(message: String) {
        val userMsg = ChatMessage(text = message, isUser = true)
        _chatHistory.value = _chatHistory.value + userMsg
        _isThinking.value = true

        viewModelScope.launch {
            try {
                // Build simple context with sensor state
                val currentState = sensorState.value
                val contextPrompt = """
                    Current Vehicle State:
                    Vehicle: ${currentState.vehicleName} (${currentState.vehicleId})
                    Driver: ${currentState.driverName}
                    Location: LAT ${currentState.latitude}, LNG ${currentState.longitude}
                    Temp: ${currentState.engineTemp}°C, Battery: ${currentState.batteryHealth}%
                    Fuel: ${currentState.fuelLevel}%, RPM: ${currentState.rpm}, Speed: ${currentState.speed}km/h
                    Tire Pressures (FL, FR, RL, RR): ${currentState.tirePressureFL}, ${currentState.tirePressureFR}, ${currentState.tirePressureRL}, ${currentState.tirePressureRR} PSI
                    Safety Score: ${currentState.driverSafetyScore}, Efficiency: ${currentState.driverEfficiencyScore}
                    Active OBD-II DTCs: ${currentState.activeDtcs.joinToString(", ") { it.code }}
                    
                    User question: $message
                    
                    Respond as an intelligent vehicle assistant, capable of diagnosing OBD-II issues. Keep answers concise, professional, and act as an enterprise fleet assistant.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = contextPrompt)))
                    )
                )
                
                val response = RetrofitClient.service.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )
                
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am unable to process that at the moment."
                _chatHistory.value = _chatHistory.value + ChatMessage(text = replyText, isUser = false)
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(text = "Error connecting to AI Assistant: ${e.message}", isUser = false)
            } finally {
                _isThinking.value = false
            }
        }
    }

    init {
        // Clear old database data on startup for a fresh demo experience
        viewModelScope.launch {
            repository.clearAlerts()
            repository.clearPredictions()
            simulationEngine.startSimulation()
        }
    }

    fun triggerEvent(type: String) {
        when(type) {
            "overheat" -> simulationEngine.forceOverheat = true
            "tire" -> simulationEngine.forceTireDrop = true
            "battery" -> simulationEngine.forceBatteryDrain = true
            "brake" -> simulationEngine.forceHarshBrake = true
            "reset" -> {
                simulationEngine.forceOverheat = false
                simulationEngine.forceTireDrop = false
                simulationEngine.forceBatteryDrain = false
                simulationEngine.forceHarshBrake = false
                viewModelScope.launch { repository.clearAlerts(); repository.clearPredictions() }
            }
        }
    }
}
