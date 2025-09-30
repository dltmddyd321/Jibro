package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.CheckStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteStationUseCase(
    private val repository: CheckStationRepository
) {
    suspend operator fun invoke(id: String) = withContext(Dispatchers.IO) {
        repository.deleteStationList(id)
    }
}
