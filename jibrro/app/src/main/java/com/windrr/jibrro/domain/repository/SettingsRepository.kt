package com.windrr.jibrro.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getLastTrainNotification(): Flow<Boolean>
    suspend fun setLastTrainNotification(enabled: Boolean)
}