package com.windrr.jibrro.data.repository.repositoryImpl

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.domain.repository.AlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmRepository {
    @SuppressLint("ScheduleExactAlarm")
    override suspend fun registerAlarm(alarmInfo: AlarmInfo): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return false
        val intent = Intent(context, com.windrr.jibrro.presentation.alarm.AlarmReceiver::class.java).apply {
            data = Uri.Builder().scheme("jibro").authority("last-train")
                .appendPath(alarmInfo.arrivalInfo.hashCode().toString()).build()
            putExtra("arrivalInfo", alarmInfo.arrivalInfo)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmInfo.time, pendingIntent)
            true
        } catch (_: SecurityException) {
            // Android can revoke exact-alarm access between checking and scheduling.
            false
        }
    }
}
