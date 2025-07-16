package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.repository.StationRepository
import javax.inject.Inject

class GetStationListUseCase @Inject constructor(
    private val repository: StationRepository
) {
    operator fun invoke(): List<SubwayStation> = repository.getSubwayStations()
}