package com.windrr.jibrro.data.repository.repositoryImpl

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.data.repository.datasource.StationDataSource
import javax.inject.Inject

class SubwayStationRepositoryImpl @Inject constructor(
    private val assetDataSource: StationDataSource
) : StationRepository {
    override fun getSubwayStations(): List<SubwayStation> {
        return assetDataSource.loadStations()
    }
}