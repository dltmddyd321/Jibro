package com.windrr.jibrro.util

import android.content.Context
import com.google.gson.Gson
import com.windrr.jibrro.data.model.StationListWrapper
import com.windrr.jibrro.data.model.SubwayStation

fun loadStationList(context: Context): List<SubwayStation> {
    val assetManager = context.assets
    val inputStream = assetManager.open("stationList.json")
    val json = inputStream.bufferedReader().use { it.readText() }

    val gson = Gson()
    val wrapper = gson.fromJson(json, StationListWrapper::class.java)
    return wrapper.data
}
