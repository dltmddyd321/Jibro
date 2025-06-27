package com.windrr.jibrro.data.respository.datasource

import android.content.Context
import com.google.gson.Gson
import com.windrr.jibrro.data.model.StationListWrapper
import com.windrr.jibrro.data.model.SubwayStation
import javax.inject.Inject

class StationAssetDataSource @Inject constructor(
    private val context: Context
) {
    fun loadStations(): List<SubwayStation> {
        val inputStream = context.assets.open("stationList.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val gson = Gson()
        val wrapper = gson.fromJson(json, StationListWrapper::class.java)
        return wrapper.data
    }
}