package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.domain.repository.CheckStationRepository
import kotlinx.coroutines.flow.Flow

class GetCheckStationListUseCase(
    private val repository: CheckStationRepository
) {
    operator fun invoke(): Flow<List<CheckStation>> = repository.getStationList()
}
