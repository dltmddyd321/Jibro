package com.windrr.jibrro.data.respository.datasource

import com.windrr.jibrro.data.model.SubwayArrivalResponse
import retrofit2.Response

interface SubwayArrivalRemoteDataSource {
    suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Response<SubwayArrivalResponse>
}