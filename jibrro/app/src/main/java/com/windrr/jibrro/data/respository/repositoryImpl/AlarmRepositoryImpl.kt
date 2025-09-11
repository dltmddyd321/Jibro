package com.windrr.jibrro.data.respository.repositoryImpl

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.domain.repository.AlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmRepository {
    @SuppressLint("ScheduleExactAlarm")
    override suspend fun registerAlarm(alarmInfo: AlarmInfo) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.windrr.jibrro.presentation.alarm.AlarmReceiver::class.java).apply {
            putExtra("arrivalInfo", alarmInfo.arrivalInfo)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmInfo.time,
            pendingIntent
        )
    }
}
