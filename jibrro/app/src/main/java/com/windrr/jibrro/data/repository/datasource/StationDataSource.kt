package com.windrr.jibrro.data.repository.datasource

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.windrr.jibrro.data.model.StationListWrapper
import com.windrr.jibrro.data.model.SubwayStation
import javax.inject.Inject

class StationAssetDataSource @Inject constructor(
    private val context: Context
) {
    fun loadStations(): List<SubwayStation> {
        return try {
            val inputStream = context.assets.open("stationList.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            Log.d("StationLoader", "Loaded JSON: ${json.take(100)}...")
            val gson = Gson()
            val wrapper = gson.fromJson(json, StationListWrapper::class.java)
            Log.d("StationLoader", "Loaded ${wrapper.data.size} stations")
            val stations = wrapper.data.map { it.name }
            Log.d("StationLoader", "Loaded stations: ${stations.contains("삼동")}")
            wrapper.data
        } catch (e: Exception) {
            Log.e("StationLoader", "Error loading stations", e)
            emptyList()
        }
    }
}