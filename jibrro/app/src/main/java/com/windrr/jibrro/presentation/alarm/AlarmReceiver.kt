package com.windrr.jibrro.presentation.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.windrr.jibrro.R
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.infrastructure.AlertFeature
import com.windrr.jibrro.infrastructure.alertPermissions
import com.windrr.jibrro.infrastructure.ensureAlertChannels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var getLastTrainNotification: GetLastTrainNotificationUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = withTimeoutOrNull(3_000) { getLastTrainNotification().first() } == true
                if (enabled && context.alertPermissions(AlertFeature.LAST_TRAIN).notifications) {
                    showNotification(context, intent)
                }
            } catch (_: java.io.IOException) {
                // DataStore may be unavailable during system shutdown.
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, intent: Intent) {
        val channelId = AlertFeature.LAST_TRAIN.channelId
        val title = "막차 도착 안내"
        val content = intent.getStringExtra("arrivalInfo") ?: "인근 지하철 막차 운행입니다."

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        context.ensureAlertChannels()

        val launchIntent = Intent(
            context,
            com.windrr.jibrro.presentation.activity.MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}
