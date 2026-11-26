package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.repository.StationRepository
class StationSearchUseCase(
    private val stationRepository: StationRepository
) {
    fun searchByKeyword(keyword: String): List<SubwayStation> {
        val list = stationRepository.getSubwayStations()
        if (keyword.isEmpty()) return list
        return list.filter { it.name.contains(keyword) }
    }
}
