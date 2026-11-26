package com.windrr.jibrro.infrastructure

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

internal enum class AlertFeature(val title: String, val channelId: String) {
    DESTINATION("목적지 알림", "destination_alert_channel"),
    LAST_TRAIN("막차 알림", "alarm_channel")
}

internal data class AlertPermissions(
    val preciseLocation: Boolean,
    val backgroundLocation: Boolean,
    val notifications: Boolean,
    val exactAlarm: Boolean
) {
    val ready: Boolean get() = preciseLocation && backgroundLocation && notifications && exactAlarm
}

internal fun Context.hasPreciseLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

internal fun Context.alertPermissions(feature: AlertFeature): AlertPermissions {
    val channelEnabled = isAlertChannelEnabled(feature)
    return AlertPermissions(
        preciseLocation = hasPreciseLocationPermission(),
        backgroundLocation = feature != AlertFeature.LAST_TRAIN || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED,
        notifications = canPostAlerts() && channelEnabled,
        exactAlarm = feature != AlertFeature.LAST_TRAIN || canScheduleReminders()
    )
}


internal fun Context.canPostAlerts(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

internal fun Context.canScheduleReminders(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
    getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

internal fun Context.isAlertChannelEnabled(feature: AlertFeature): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
        getSystemService(NotificationManager::class.java).getNotificationChannel(feature.channelId)?.importance !=
        NotificationManager.IMPORTANCE_NONE

internal fun Context.ensureAlertChannels() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(NotificationManager::class.java)
    AlertFeature.entries.forEach { feature ->
        manager.createNotificationChannel(
            NotificationChannel(feature.channelId, feature.title, NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
            }
        )
    }
}
