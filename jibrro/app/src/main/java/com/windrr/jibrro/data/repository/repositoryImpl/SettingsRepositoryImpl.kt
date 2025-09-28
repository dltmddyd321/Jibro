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
}