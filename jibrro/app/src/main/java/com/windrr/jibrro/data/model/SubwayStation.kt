package com.windrr.jibrro.data.model

import com.google.gson.annotations.SerializedName

data class SubwayStation(
    val line: String,
    val name: String,
    val station_nm_chn: String,
    val station_nm_jpn: String,
    val station_nm_eng: String,
    val fr_code: String,
    val station_cd: String,
    val bldn_id: String,
    val lat: Double,
    val lng: Double
)

data class StationListWrapper(
    @SerializedName("DATA")
    val data: List<SubwayStation>
)