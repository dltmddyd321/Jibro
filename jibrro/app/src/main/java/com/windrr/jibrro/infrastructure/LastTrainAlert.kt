package com.windrr.jibrro.infrastructure

import android.content.Context
import com.windrr.jibrro.data.commute.subwayArrival
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LastTrainAlert @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enabled: GetLastTrainNotificationUseCase,
    private val registerAlarm: RegisterAlarmUseCase
) {
    private val mutex = Mutex()
    private val sent = context.getSharedPreferences("notified_last_trains", Context.MODE_PRIVATE)

    suspend fun check(station: String, rows: List<RealtimeArrival>) = withContext(Dispatchers.IO) {
        if (rows.none { it.lstcarAt == "1" }) return@withContext
        mutex.withLock {
            val permissions = context.alertPermissions(AlertFeature.LAST_TRAIN)
            if (!permissions.notifications || !permissions.exactAlarm || !enabled().first()) return@withLock

            val now = System.currentTimeMillis()
            val cutoff = now - 8 * 60 * 60 * 1_000L
            val stationName = if (station == "서울") "서울역" else station
            val candidates = rows.filter { it.lstcarAt == "1" && subwayArrival(it, now) != null }
                .associateBy { row ->
                    listOf(stationName, row.subwayId, row.btrainNo, row.updnLine, row.bstatnNm).joinToString("|")
                }.filterKeys { sent.getLong(it, 0L) < cutoff }
            if (candidates.isEmpty()) return@withLock

            val displayName = if (stationName.endsWith("역")) stationName else "${stationName}역"
            val directions = candidates.values.mapNotNull { it.trainLineNm?.takeIf(String::isNotBlank) }
                .distinct().joinToString(" · ")
            val message = if (directions.isBlank()) displayName else "$displayName · $directions"
            if (!registerAlarm(AlarmInfo(now, message))) return@withLock

            val edit = sent.edit()
            sent.all.filterValues { (it as? Long ?: 0L) < cutoff }.keys.forEach { edit.remove(it) }
            candidates.keys.forEach { edit.putLong(it, now) }
            edit.apply()
        }
    }
}
