package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetLastTrainNotificationUseCase(
    private val repo: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.getLastTrainNotification()
}