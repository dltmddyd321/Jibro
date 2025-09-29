package com.windrr.jibrro.data.repository.datasource

import com.windrr.jibrro.data.model.Destination
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    val lastTrainNotification: Flow<Boolean>
    suspend fun setLastTrainNotification(enabled: Boolean)

    val destination: Flow<Destination?>
    suspend fun setDestination(destination: Destination?)

    val lastLat: Flow<Double?>
    suspend fun setLastLat(value: Double)

    val lastLng: Flow<Double?>
    suspend fun setLastLng(value: Double)
}