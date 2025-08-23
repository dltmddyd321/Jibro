package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetDestinationUseCase(
    private val repo: SettingsRepository
) {
    operator fun invoke(): Flow<Destination?> = repo.getDestination()
}