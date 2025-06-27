package com.windrr.jibrro.data.respository

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.data.respository.datasource.StationAssetDataSource
import javax.inject.Inject

class SubwayStationRepositoryImpl @Inject constructor(
    private val assetDataSource: StationAssetDataSource
) : StationRepository {
    override fun getSubwayStations(): List<SubwayStation> {
        return assetDataSource.loadStationList()
    }
}