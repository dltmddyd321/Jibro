package com.windrr.jibrro.data.repository.repositoryImpl

import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.data.repository.datasource.SubwayLocalDataSource
import com.windrr.jibrro.domain.repository.CheckStationRepository
import kotlinx.coroutines.flow.Flow

class CheckStationRepositoryImpl(
    private val subwayLocalDataSource: SubwayLocalDataSource
): CheckStationRepository {
    override suspend fun saveStationList(stations: List<CheckStation>) {
        subwayLocalDataSource.saveSubwayList(stations)
    }

    override fun getStationList(): Flow<List<CheckStation>> {
        return subwayLocalDataSource.getSubwayList()
    }

    override suspend fun deleteStationList(id: String) {
        subwayLocalDataSource.deleteSubwayList(id)
    }
}