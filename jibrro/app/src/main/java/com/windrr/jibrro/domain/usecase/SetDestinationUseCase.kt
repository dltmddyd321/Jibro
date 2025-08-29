package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.domain.repository.SettingsRepository

class SetDestinationUseCase(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(destination: Destination?) = repo.setDestination(destination)
}