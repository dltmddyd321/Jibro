package com.windrr.jibrro.data.respository.repositoryImpl

import android.content.Context
import com.windrr.jibrro.data.util.SettingsDataStore
import com.windrr.jibrro.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    override fun getLastTrainNotification(): Flow<Boolean> {
        return SettingsDataStore.getLastTrainNotificationFlow(context)
    }

    override suspend fun setLastTrainNotification(enabled: Boolean) {
        SettingsDataStore.setLastTrainNotification(context, enabled)
    }

    override fun getDestination(): Flow<String> {
        return SettingsDataStore.getDestinationFlow(context)
    }

    override suspend fun setDestination(destination: String) {
        SettingsDataStore.setDestination(context, destination)
    }
}