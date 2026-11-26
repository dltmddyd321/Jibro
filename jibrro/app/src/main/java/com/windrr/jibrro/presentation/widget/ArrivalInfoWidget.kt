package com.windrr.jibrro.presentation.widget

import android.content.Context
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.windrr.jibrro.presentation.widget.di.WidgetEntryPoint
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ArrivalInfoWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId): Nothing = coroutineScope {
        val data = WidgetArrivals(context, EntryPoints.get(context, WidgetEntryPoint::class.java))
        val snapshot = MutableStateFlow(WidgetSnapshot())
        val requests = MutableStateFlow<WidgetRequest?>(null)
        launch {
            requests.filterNotNull().collectLatest { request ->
                var pending = snapshot.value.copy(arrivals = emptyList(), fetchedAt = null, loading = true, failed = false)
                snapshot.value = pending
                snapshot.value = try {
                    withTimeoutOrNull(10_000) {
                        val stops = data.stops()
                        val selected = stops.firstOrNull { it.favorite && it.name == request.favorite }
                        pending = WidgetSnapshot(selected ?: stops.firstOrNull(), stops)
                        snapshot.value = pending
                        data.arrivals(pending)
                    } ?: pending.copy(loading = false, failed = true)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    pending.copy(loading = false, failed = true)
                }
            }
        }
        provideContent {
            val preferences = currentState<Preferences>()
            val request = WidgetRequest(preferences[REFRESH_REQUEST] ?: 0L, preferences[FAVORITE_STATION])
            val state by snapshot.collectAsState()
            // Both launcher orientations can compose together. Share one request between them.
            SideEffect { requests.value = request }
            ArrivalWidgetContent(state, id.toString())
        }
    }

    internal suspend fun refresh(context: Context, id: GlanceId, station: WidgetStop? = null) {
        updateAppWidgetState(context, id) { preferences ->
            if (station != null) {
                if (station.favorite) preferences[FAVORITE_STATION] = station.name
                else preferences.remove(FAVORITE_STATION)
            }
            preferences[REFRESH_REQUEST] = (preferences[REFRESH_REQUEST] ?: 0L) + 1L
        }
        update(context, id)
    }

    private data class WidgetRequest(val revision: Long, val favorite: String?)

    private companion object {
        val REFRESH_REQUEST = longPreferencesKey("arrival_refresh")
        val FAVORITE_STATION = stringPreferencesKey("arrival_favorite_station")
    }
}
