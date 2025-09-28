package com.windrr.jibrro.data.repository.datasourceImpl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.data.repository.datasource.SettingsLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsLocalDataSource {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val LAST_TRAIN_KEY = booleanPreferencesKey("last_train_enabled")
    private val DEST_ID = stringPreferencesKey("destination_id")
    private val DEST_NAME = stringPreferencesKey("destination_name")
    private val DEST_LAT = doublePreferencesKey("destination_lat")
    private val DEST_LNG = doublePreferencesKey("destination_lng")

    override val lastTrainNotification: Flow<Boolean> =
        context.dataStore.data
            .map { preferences -> preferences[LAST_TRAIN_KEY] ?: false }

    override suspend fun setLastTrainNotification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TRAIN_KEY] = enabled
        }
    }

    override val destination: Flow<Destination?> =
        context.dataStore.data
            .map { prefs ->
                val id = prefs[DEST_ID] ?: return@map null
                val name = prefs[DEST_NAME] ?: return@map null
                val lat = prefs[DEST_LAT] ?: return@map null
                val lng = prefs[DEST_LNG] ?: return@map null
                Destination(id, name, lat, lng)
            }

    override suspend fun setDestination(destination: Destination?) {
        context.dataStore.edit { prefs ->
            if (destination == null) {
                prefs.remove(DEST_ID)
                prefs.remove(DEST_NAME)
                prefs.remove(DEST_LAT)
                prefs.remove(DEST_LNG)
            } else {
                prefs[DEST_ID] = destination.id
                prefs[DEST_NAME] = destination.name
                prefs[DEST_LAT] = destination.latitude
                prefs[DEST_LNG] = destination.longitude
            }
        }
    }
}