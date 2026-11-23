package com.windrr.jibrro.data.model

import com.google.gson.annotations.SerializedName

data class StationListWrapper(
    @SerializedName("DATA")
    val data: List<SubwayStation>
)
