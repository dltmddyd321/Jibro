package com.windrr.jibrro.data.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import com.windrr.jibrro.infrastructure.LocationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltWorker
class JibroWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val getStationListUseCase: GetStationListUseCase,
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase,
    private val registerAlarmUseCase: RegisterAlarmUseCase,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val location = locationHelper.getLastLocationSuspend()
        if (location == null) {
            Log.e("JibroWorker", "위치를 가져올 수 없습니다.")
            return Result.failure()
        }

        val (lat, lng) = location

        val stations = getStationListUseCase()
        val closest = stations.minByOrNull {
            distanceInMeters(lat, lng, it.lat, it.lng)
        }

        val stationName = closest?.name.orEmpty()
        Log.d("JibroWorker", "가장 가까운 역: $stationName")

        val arrivalDataList = getSubwayArrivalDataUseCase.execute(statnNm = stationName).data ?: emptyList()

        if (arrivalDataList.any { it.lstcarAt == "1" }) {
            registerAlarmUseCase.invoke(System.currentTimeMillis())
        }

        return Result.success()
    }

    private fun distanceInMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}