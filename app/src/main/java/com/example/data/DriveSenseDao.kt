package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveSenseDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<Alert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert)

    @Query("DELETE FROM alerts")
    suspend fun clearAlerts()

    @Query("SELECT * FROM predictions ORDER BY failureProbability DESC")
    fun getAllPredictions(): Flow<List<Prediction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction)

    @Query("SELECT * FROM predictions WHERE component = :component LIMIT 1")
    suspend fun getPrediction(component: String): Prediction?

    @Query("DELETE FROM predictions")
    suspend fun clearPredictions()
}
