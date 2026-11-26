package com.windrr.jibrro.infrastructure

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.util.distanceInMeters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

@HiltWorker
internal class JibroWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val getStationListUseCase: GetStationListUseCase,
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase,
    private val lastTrainAlert: LastTrainAlert,
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
        try {
            val query = if (station.name == "서울역") "서울" else station.name
            val arrivals = getSubwayArrivalDataUseCase.execute(statnNm = query).data.orEmpty()
            lastTrainAlert.check(station.name, arrivals)
        } catch (_: IOException) {
            return Result.retry()
        }
        return Result.success()
    }
}


internal fun enqueueLastTrainCheck(context: Context) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        "CheckLastTrainNow",
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<JibroWorker>().build()
    )
}
