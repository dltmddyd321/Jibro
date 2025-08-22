package com.windrr.jibrro.infrastructure

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.windrr.jibrro.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var locationHelper: LocationHelper

    private val locationUpdateInterval = 5000L

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startLocationUpdates()
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
                    Log.d("LocationService", "Current location: $lat, $lon")
                    // 필요하면 서버 전송, DB 저장 등 추가
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