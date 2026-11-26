package com.windrr.jibrro.presentation.widget

import SubwayLineMap
import android.content.Context
import com.windrr.jibrro.data.commute.subwayArrival
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.commute.Coordinates
import com.windrr.jibrro.domain.state.LocationState
import com.windrr.jibrro.domain.state.Result
import com.windrr.jibrro.infrastructure.LocationHelper
import com.windrr.jibrro.presentation.widget.di.WidgetEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

internal data class WidgetStop(val name: String, val favorite: Boolean)
internal data class WidgetArrival(
    val line: String,
    val direction: String,
    val destination: String,
    val eta: String?,
    val stops: String?,
    val lastTrain: Boolean
)

internal data class WidgetSnapshot(
    val stop: WidgetStop? = null,
    val stops: List<WidgetStop> = emptyList(),
    val arrivals: List<WidgetArrival> = emptyList(),
    val fetchedAt: Long? = null,
    val loading: Boolean = true,
    val failed: Boolean = false
)

internal class WidgetArrivals(context: Context, private val entryPoint: WidgetEntryPoint) {
    private val location = LocationHelper(context)

    suspend fun stops(): List<WidgetStop> {
        val point = withTimeoutOrNull(1_000) { location.getLastLocationSuspend() }
            ?.let { (lat, lng) -> Coordinates(lat, lng) }?.takeIf { it.isValid }
            ?: (entryPoint.getLastLocationUseCase().invoke() as? LocationState.Available)
                ?.let { Coordinates(it.lat, it.lng) }?.takeIf { it.isValid }
        val closest = point?.let { entryPoint.getClosestStationUseCase().invoke(it.latitude, it.longitude) }
        val favorites = entryPoint.getCheckStationListUseCase().invoke().first()
            .map { it.name }.distinct().sorted()
        return listOfNotNull(closest?.let { WidgetStop(it.name, favorite = false) }) +
            favorites.map { WidgetStop(it, favorite = true) }
    }

    suspend fun arrivals(snapshot: WidgetSnapshot): WidgetSnapshot {
        val stop = snapshot.stop ?: return snapshot.copy(loading = false)
        val name = if (stop.name == "서울역") "서울" else stop.name
        val result = entryPoint.getSubwayArrivalDataUseCase().execute(statnNm = name)
        val now = System.currentTimeMillis()
        return when (result) {
            is Result.Success -> snapshot.copy(
                arrivals = widgetArrivals(result.data.orEmpty(), now),
                fetchedAt = now,
                loading = false
            )
            else -> snapshot.copy(loading = false, failed = true)
        }
    }
}

internal fun widgetArrivals(rows: List<RealtimeArrival>, now: Long): List<WidgetArrival> {
    val valid = rows.mapNotNull { row -> subwayArrival(row, now)?.let { row to it } }.sortedWith(
        compareBy({ (_, arrival) -> arrival.seconds ?: Int.MAX_VALUE }, { (_, arrival) -> arrival.stopsAway ?: Int.MAX_VALUE })
    )
    // Show a train from each direction before listing any additional trains.
    val directions = mutableSetOf<Pair<String, String>>()
    val (leading, others) = valid.partition { (row, _) ->
        directions.add(row.subwayId.orEmpty() to row.updnLine.orEmpty())
    }
    return (leading + others).map { (row, arrival) ->
        val eta = arrival.seconds?.let { seconds ->
            when {
                seconds == 0 -> "곧 도착"
                seconds < 60 -> "${seconds}초"
                seconds % 60 == 0 -> "${seconds / 60}분"
                else -> "${seconds / 60}분 ${seconds % 60}초"
            }
        }
        val direction = when (row.updnLine) {
            "상행" -> "↑ 상행"
            "하행" -> "↓ 하행"
            "내선" -> "↻ 내선"
            "외선" -> "↺ 외선"
            else -> row.updnLine.orEmpty()
        }
        WidgetArrival(
            SubwayLineMap.getNameById(row.subwayId.orEmpty()).ifBlank { "지하철" },
            direction,
            row.bstatnNm.orEmpty().takeIf { it.isNotBlank() }?.plus("행").orEmpty(),
            eta,
            arrival.stopsAway?.takeIf { it > 0 }?.let { "${it}정거장 전" },
            row.lstcarAt == "1"
        )
    }
}
