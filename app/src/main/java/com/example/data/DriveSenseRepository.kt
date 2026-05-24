package com.example.data

import kotlinx.coroutines.flow.Flow

class DriveSenseRepository(private val dao: DriveSenseDao) {
    val alerts: Flow<List<Alert>> = dao.getAllAlerts()
    val predictions: Flow<List<Prediction>> = dao.getAllPredictions()

    suspend fun addAlert(alert: Alert) = dao.insertAlert(alert)
    suspend fun clearAlerts() = dao.clearAlerts()

    suspend fun addPrediction(prediction: Prediction) = dao.insertPrediction(prediction)
    suspend fun clearPredictions() = dao.clearPredictions()
    suspend fun getPrediction(component: String) = dao.getPrediction(component)
}
