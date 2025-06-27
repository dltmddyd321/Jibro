package com.windrr.jibrro.data.respository

import com.windrr.jibrro.data.model.SubwayStation

interface StationRepository {
    fun getSubwayStations(): List<SubwayStation>
}