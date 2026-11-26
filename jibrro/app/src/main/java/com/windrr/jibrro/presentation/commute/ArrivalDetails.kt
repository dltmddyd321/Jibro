package com.windrr.jibrro.presentation.commute

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.windrr.jibrro.domain.commute.BoardingArrival
import com.windrr.jibrro.domain.commute.TransitMode
import com.windrr.jibrro.presentation.activity.ArrivalDetailActivity
import java.io.Serializable

internal data class ArrivalDetails(
    val stop: String,
    val line: String,
    val bus: Boolean = false,
    val direction: String? = null,
    val destination: String? = null,
    val alightingStop: String? = null,
    val eta: String? = null,
    val stopsAway: String? = null,
    val lastTrain: Boolean = false,
    val fetchedAt: Long? = null
) : Serializable {
    fun intent(context: Context, widgetRowKey: String? = null): Intent =
        Intent(context, ArrivalDetailActivity::class.java).putExtra(EXTRA, this).apply {
            // Glance's PendingIntents distinguish rows by URI. Extras alone are ignored.
            if (widgetRowKey != null) {
                data = Uri.Builder().scheme("jibro").authority("arrival").appendPath(widgetRowKey).build()
            }
        }

    companion object {
        const val EXTRA = "com.windrr.jibrro.ARRIVAL_DETAILS"
        private const val serialVersionUID = 1L

        fun from(
            arrival: BoardingArrival,
            mode: TransitMode,
            stop: String,
            line: String,
            destination: String? = null,
            alightingStop: String? = null,
            direction: String? = null,
            lastTrain: Boolean = false
        ) = ArrivalDetails(
            stop = stop,
            line = line,
            bus = mode == TransitMode.BUS,
            direction = arrival.direction?.takeIf(String::isNotBlank) ?: direction,
            destination = destination,
            alightingStop = alightingStop,
            eta = arrival.status?.takeIf(String::isNotBlank)
                ?: arrival.seconds?.takeIf { it >= 0 }?.let(::formatArrivalDuration),
            stopsAway = arrival.stopsAway?.takeIf { it > 0 }?.let { "${it}정거장 전" },
            lastTrain = lastTrain
        )
    }
}

internal fun formatArrivalDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return when {
        seconds == 0 -> "곧 도착"
        minutes == 0 -> "약 ${remaining}초"
        remaining == 0 -> "약 ${minutes}분"
        else -> "약 ${minutes}분 ${remaining}초"
    }
}
