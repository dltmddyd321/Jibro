package com.windrr.jibrro.data.api

import com.windrr.jibrro.data.model.SubwayArrivalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayApiService {
    @GET("api/subway/{apiKey}/xml/realtimeStationArrival/{startIndex}/{endIndex}/{stationName}")
    fun getSubwayArrivalData(
        @Path("KEY") apiKey: String,
        @Path("START_INDEX") startIndex: Int,
        @Path("END_INDEX") endIndex: Int,
        @Path("statnNm") statnNm: String
    ): Response<SubwayArrivalResponse>
}