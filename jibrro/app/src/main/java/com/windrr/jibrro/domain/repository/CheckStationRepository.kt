package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.CheckStation
import kotlinx.coroutines.flow.Flow

interface CheckStationRepository {
    suspend fun saveStationList(stations: List<CheckStation>)
    fun getStationList(): Flow<List<CheckStation>>
    suspend fun deleteStationList(id: String)
}