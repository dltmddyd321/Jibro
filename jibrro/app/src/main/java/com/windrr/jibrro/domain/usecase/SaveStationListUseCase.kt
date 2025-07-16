package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.domain.repository.CheckStationRepository

class SaveStationListUseCase(
    private val repository: CheckStationRepository
) {
    suspend operator fun invoke(stations: List<CheckStation>) {
        repository.saveStationList(stations)
    }
}
