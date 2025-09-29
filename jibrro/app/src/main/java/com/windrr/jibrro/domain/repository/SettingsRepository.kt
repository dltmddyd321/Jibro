package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.Destination
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getLastTrainNotification(): Flow<Boolean>
    suspend fun setLastTrainNotification(enabled: Boolean)

    fun getDestination(): Flow<Destination?>
    suspend fun setDestination(destination: Destination?)

    fun getLastLat(): Flow<Double?>
    suspend fun setLastLat(lat: Double)

    fun getLastLng(): Flow<Double?>
    suspend fun setLastLng(lng: Double)
}