package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.CheckStationRepository

class DeleteStationUseCase(
    private val repository: CheckStationRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteStationList(id)
    }
}
