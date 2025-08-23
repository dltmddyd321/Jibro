package com.windrr.jibrro.infrastructure

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.windrr.jibrro.R
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.domain.usecase.GetDestinationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var locationHelper: LocationHelper

    @Inject
    lateinit var getDestinationUseCase: GetDestinationUseCase

    private val locationUpdateInterval = 5000L

    private var currentDestination: Destination? = null

    //백그라운드에서 반복 실행하는 작업을 제어
    //단, 하위 코루틴 예외로 전체 Scope가 취소되지 않게 구성
    private val job = SupervisorJob()
    //나중에 서비스가 종료될 때 한 번에 모든 코루틴을 취소
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        scope.launch(Dispatchers.IO) {
            currentDestination = getDestinationUseCase().firstOrNull()
            withContext(Dispatchers.Main) {
                startForegroundService()
                startLocationUpdates()
            }
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("실시간 위치 추적 중")
            .setSmallIcon(R.drawable.jibro_text)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        scope.launch {
            while (isActive) {
                locationHelper.getLastLocationSuspend()?.let { (lat, lon) ->
                    val destinationLat = currentDestination?.latitude ?: return@let
                    val destinationLng = currentDestination?.longitude ?: return@let

                    val results = FloatArray(1)
                    Location.distanceBetween(lat, lon, destinationLat, destinationLng, results)
                    val distanceMeters = results[0]

                    when {
                        distanceMeters <= 500 -> {

                        }
                        distanceMeters <= 1000 -> {

                        }
                        distanceMeters <= 2000 -> {

                        }
                        else -> {
                            //알람 취소?
                        }
                    }
                }
                delay(locationUpdateInterval)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}