package com.windrr.jibrro.data.repository.repositoryImpl

import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.data.repository.datasource.SettingsLocalDataSource
import com.windrr.jibrro.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val localDataSource: SettingsLocalDataSource
) : SettingsRepository {
    override fun getLastTrainNotification(): Flow<Boolean> {
        return localDataSource.lastTrainNotification
    }

    override suspend fun setLastTrainNotification(enabled: Boolean) {
        localDataSource.setLastTrainNotification(enabled)
    }

    override fun getDestination(): Flow<Destination?> {
        return localDataSource.destination
    }

    override suspend fun setDestination(destination: Destination?) {
        localDataSource.setDestination(destination)
    }

    override fun getLastLat(): Flow<Double?> {
        return localDataSource.lastLat
    }

    override suspend fun setLastLat(lat: Double) {
        localDataSource.setLastLat(lat)
    }

    override fun getLastLng(): Flow<Double?> {
        return localDataSource.lastLng
    }

    override suspend fun setLastLng(lng: Double) {
        localDataSource.setLastLng(lng)
    }
}