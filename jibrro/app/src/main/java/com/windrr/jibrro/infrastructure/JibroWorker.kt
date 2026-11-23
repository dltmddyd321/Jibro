package com.windrr.jibrro.infrastructure

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.windrr.jibrro.data.commute.subwayArrival
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import com.windrr.jibrro.util.distanceInMeters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

@HiltWorker
class JibroWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val getStationListUseCase: GetStationListUseCase,
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase,
    private val registerAlarmUseCase: RegisterAlarmUseCase,
    private val getLastTrainNotification: GetLastTrainNotificationUseCase
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        if (!getLastTrainNotification().first()) return Result.success()
        if (!applicationContext.alertPermissions(AlertFeature.LAST_TRAIN).ready) return Result.success()
        val point = withTimeoutOrNull(15_000) { locationHelper.currentCoordinates() } ?: return Result.success()

        val station = getStationListUseCase().asSequence()
            .filter { it.lat != 0.0 && it.lng != 0.0 }
            .minByOrNull { distanceInMeters(point.latitude, point.longitude, it.lat, it.lng) }
            ?: return Result.success()
        val arrivals = try {
            getSubwayArrivalDataUseCase.execute(statnNm = station.name).data.orEmpty()
        } catch (_: IOException) {
            return Result.retry()
        }

        val now = System.currentTimeMillis()
        val lastTrain = arrivals.firstOrNull { it.lstcarAt == "1" && subwayArrival(it, now) != null }
            ?: return Result.success()
        registerAlarmUseCase(AlarmInfo(now, "${station.name}역 · ${lastTrain.trainLineNm}"))
        return Result.success()
    }
}
