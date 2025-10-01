package com.windrr.jibrro.data.repository.datasource

import com.windrr.jibrro.data.model.SubwayStation

interface StationDataSource {
    fun loadStations(): List<SubwayStation>
}