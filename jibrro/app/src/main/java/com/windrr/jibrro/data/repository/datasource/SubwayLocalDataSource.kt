package com.windrr.jibrro.data.repository.datasource

import com.windrr.jibrro.data.model.CheckStation
import kotlinx.coroutines.flow.Flow

interface SubwayLocalDataSource {
    suspend fun saveSubwayList(stations: List<CheckStation>)

    fun getSubwayList(): Flow<List<CheckStation>>

    suspend fun deleteSubwayList(id: String)
}