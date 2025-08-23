package com.windrr.jibrro.data.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.windrr.jibrro.data.model.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val LAST_TRAIN_KEY = booleanPreferencesKey("last_train_enabled")
    private val DEST_ID = stringPreferencesKey("destination_id")
    private val DEST_NAME = stringPreferencesKey("destination_name")
    private val DEST_LAT = doublePreferencesKey("destination_lat")
    private val DEST_LNG = doublePreferencesKey("destination_lng")

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

    fun getDestinationFlow(context: Context): Flow<Destination?> {
        return context.dataStore.data.map { prefs ->
            val id = prefs[DEST_ID] ?: return@map null
            val name = prefs[DEST_NAME] ?: return@map null
            val lat = prefs[DEST_LAT] ?: return@map null
            val lng = prefs[DEST_LNG] ?: return@map null
            Destination(id, name, lat, lng)
        }
    }

    suspend fun setDestination(context: Context, destination: Destination) {
        context.dataStore.edit { prefs ->
            prefs[DEST_ID] = destination.id
            prefs[DEST_NAME] = destination.name
            prefs[DEST_LAT] = destination.latitude
            prefs[DEST_LNG] = destination.longitude
        }
    }
}
