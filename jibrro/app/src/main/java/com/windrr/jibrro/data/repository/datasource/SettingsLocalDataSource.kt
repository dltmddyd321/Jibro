package com.windrr.jibrro.data.repository.datasource

import com.windrr.jibrro.data.model.Destination
import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    val lastTrainNotification: Flow<Boolean>
    suspend fun setLastTrainNotification(enabled: Boolean)

    val destination: Flow<Destination?>
    suspend fun setDestination(destination: Destination?)
}