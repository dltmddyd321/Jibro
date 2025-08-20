package com.windrr.jibrro.data.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    private val Context.dataStore by preferencesDataStore(name = "settings")

    private val LAST_TRAIN_KEY = booleanPreferencesKey("last_train_enabled")

    private val DESTINATION_KEY = stringPreferencesKey("destination_enabled")

    fun getLastTrainNotificationFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[LAST_TRAIN_KEY] ?: false
        }
    }

    suspend fun setLastTrainNotification(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LAST_TRAIN_KEY] = enabled
        }
    }

    fun getDestinationFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[DESTINATION_KEY] ?: ""
        }
    }

    suspend fun setDestination(context: Context, destination: String) {
        context.dataStore.edit { prefs ->
            prefs[DESTINATION_KEY] = destination
        }
    }
}
