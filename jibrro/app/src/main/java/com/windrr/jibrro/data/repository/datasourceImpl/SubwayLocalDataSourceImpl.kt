package com.windrr.jibrro.data.repository.datasourceImpl

import com.windrr.jibrro.data.db.SubwayDao
import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.data.repository.datasource.SubwayLocalDataSource
import kotlinx.coroutines.flow.Flow

class SubwayLocalDataSourceImpl(
    private val subwayDao: SubwayDao
):SubwayLocalDataSource {
    override suspend fun saveSubwayList(stations: List<CheckStation>) {
        stations.forEach {
            subwayDao.insert(it)
        }
    }

    override fun getSubwayList(): Flow<List<CheckStation>> {
        return subwayDao.getAllCheckStation()
    }

    override suspend fun deleteSubwayList(id: String) {
        subwayDao.deleteCheckStationById(id)
    }
}