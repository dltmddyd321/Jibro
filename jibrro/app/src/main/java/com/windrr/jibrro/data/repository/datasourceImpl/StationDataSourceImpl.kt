package com.windrr.jibrro.data.repository.datasourceImpl

import android.content.Context
import com.google.gson.Gson
import com.windrr.jibrro.data.model.StationListWrapper
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.data.repository.datasource.StationDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StationDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StationDataSource {
    override fun loadStations(): List<SubwayStation> {
        return try {
            val inputStream = context.assets.open("stationList.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val wrapper = gson.fromJson(json, StationListWrapper::class.java)
            wrapper.data
        } catch (e: Exception) {
            emptyList()
        }
    }
}