package com.windrr.jibrro.data.api

import com.windrr.jibrro.data.model.SubwayArrivalResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayApiService {
    @GET("api/subway/{KEY}/json/realtimeStationArrival/{START_INDEX}/{END_INDEX}/{statnNm}")
    suspend fun getSubwayArrivalData(
        @Path("KEY") KEY: String,
        @Path("START_INDEX") START_INDEX: Int = 0,
        @Path("END_INDEX") END_INDEX: Int = 5,
        @Path("statnNm") statnNm: String
    ): Response<SubwayArrivalResponse>
}