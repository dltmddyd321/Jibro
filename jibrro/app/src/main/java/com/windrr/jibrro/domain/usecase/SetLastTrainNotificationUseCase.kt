package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository

class SetLastTrainNotificationUseCase(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repo.setLastTrainNotification(enabled)
}