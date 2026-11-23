package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.SubwayStation

interface StationRepository {
    fun getSubwayStations(): List<SubwayStation>
}