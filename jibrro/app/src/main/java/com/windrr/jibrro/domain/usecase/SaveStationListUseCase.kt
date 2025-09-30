package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.domain.repository.CheckStationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveStationListUseCase(
    private val repository: CheckStationRepository
) {
    suspend operator fun invoke(stations: List<CheckStation>) = withContext(Dispatchers.IO) {
        repository.saveStationList(stations)
    }
}
