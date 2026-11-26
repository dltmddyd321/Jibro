package com.windrr.jibrro.infrastructure

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.windrr.jibrro.R
import com.windrr.jibrro.domain.usecase.GetDestinationUseCase
import com.windrr.jibrro.domain.usecase.SetDestinationUseCase
import com.windrr.jibrro.presentation.activity.MainActivity
import com.windrr.jibrro.util.Action.ACTION_STOP_SERVICE
import com.windrr.jibrro.util.distanceInMeters
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationForegroundService : Service() {
    @Inject lateinit var locationHelper: LocationHelper
    @Inject lateinit var getDestination: GetDestinationUseCase
    @Inject lateinit var setDestination: SetDestinationUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var monitoring: Job? = null
    private var clearing: Job? = null
    private val notifications by lazy { getSystemService(NotificationManager::class.java) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannels()
        startForeground(TRACKING_NOTIFICATION, trackingNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            if (clearing?.isActive != true) {
                clearing = scope.launch {
                    try {
                        setDestination(null)
                        stopSelf()
                    } catch (_: IOException) {
                        notifications.notify(TRACKING_NOTIFICATION, trackingNotification("알림을 끄지 못했어요. 다시 시도해 주세요."))
                    }
                }
            }
        } else if (monitoring?.isActive != true && clearing?.isActive != true) {
            monitoring = scope.launch {
                try {
                    getDestination().distinctUntilChanged().collectLatest { destination ->
                        notifications.cancel(DESTINATION_NOTIFICATION)
                        if (destination == null) {
                            stopSelf()
                        } else {
                            val tracker = DestinationApproach()
                            locationHelper.preciseLocationUpdates().collect { point ->
                                val distance = distanceInMeters(
                                    point.latitude, point.longitude, destination.latitude, destination.longitude
                                )
                                tracker.update(distance)?.let { threshold ->
                                    if (alertPermissions(AlertFeature.DESTINATION).notifications) {
                                        showDistanceNotification(destination.name, threshold)
                                    }
                                }
                            }
                        }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    stopSelf()
                }
            }
        }
        return if (intent?.action == ACTION_STOP_SERVICE) START_NOT_STICKY else START_STICKY
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        notifications.createNotificationChannel(NotificationChannel(
            TRACKING_CHANNEL, "목적지 위치 확인", NotificationManager.IMPORTANCE_LOW
        ))
        ensureAlertChannels()
    }

    private fun trackingNotification(message: String = "선택한 역과의 거리를 확인하고 있어요."): Notification =
        NotificationCompat.Builder(this, TRACKING_CHANNEL)
            .setContentTitle("목적지 알림 사용 중")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openApp())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "알림 끄기", stopAlert())
            .build()

    private fun showDistanceNotification(name: String, threshold: Int) {
        val distance = if (threshold >= 1000) "${threshold / 1000}km" else "${threshold}m"
        val notification = NotificationCompat.Builder(this, AlertFeature.DESTINATION.channelId)
            .setContentTitle("목적지에 가까워졌어요")
            .setContentText("${name}까지 ${distance} 이내예요.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setContentIntent(openApp())
            .addAction(0, "알림 끄기", stopAlert())
            .setAutoCancel(true)
            .build()
        notifications.notify(DESTINATION_NOTIFICATION, notification)
    }

    private fun openApp() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun stopAlert() = PendingIntent.getService(
        this, 0, Intent(this, LocationForegroundService::class.java).setAction(ACTION_STOP_SERVICE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    override fun onDestroy() {
        scope.cancel()
        notifications.cancel(DESTINATION_NOTIFICATION)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private companion object {
        const val TRACKING_CHANNEL = "location_channel"
        const val TRACKING_NOTIFICATION = 1
        const val DESTINATION_NOTIFICATION = 1002
    }
}

internal class DestinationApproach {
    private val thresholds = listOf(500, 1_000, 2_000)
    private val notified = mutableSetOf<Int>()

    fun update(distanceMeters: Double): Int? {
        if (!distanceMeters.isFinite() || distanceMeters < 0) return null
        // Require a wider exit boundary to avoid re-alerting from GPS jitter.
        notified.removeAll { distanceMeters > it + 100 }
        val threshold = thresholds.firstOrNull { distanceMeters <= it } ?: return null
        if (threshold in notified) return null
        // A jump into a closer band counts as entering all of the skipped bands.
        notified.addAll(thresholds.filter { it >= threshold })
        return threshold
    }
}
