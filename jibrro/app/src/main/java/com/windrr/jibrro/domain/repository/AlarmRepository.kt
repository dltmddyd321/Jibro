package com.windrr.jibrro.domain.repository

interface AlarmRepository {
    suspend fun registerAlarm(timeMillis: Long)
}
