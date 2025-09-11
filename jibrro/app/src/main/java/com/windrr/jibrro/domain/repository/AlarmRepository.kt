package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.AlarmInfo

interface AlarmRepository {
    suspend fun registerAlarm(alarmInfo: AlarmInfo)
}
