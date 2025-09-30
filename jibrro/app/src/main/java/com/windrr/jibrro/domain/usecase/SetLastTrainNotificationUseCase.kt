package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetLastTrainNotificationUseCase(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = withContext(Dispatchers.IO) {
        repo.setLastTrainNotification(enabled)
    }
}