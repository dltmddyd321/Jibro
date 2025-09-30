package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.AlarmInfo
import com.windrr.jibrro.domain.repository.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegisterAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmInfo: AlarmInfo) = withContext(Dispatchers.IO) {
        alarmRepository.registerAlarm(alarmInfo)
    }
}
