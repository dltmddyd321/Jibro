package com.windrr.jibrro.data.repository.datasourceImpl

import com.windrr.jibrro.data.api.SubwayApiService
import com.windrr.jibrro.data.model.SubwayArrivalResponse
import com.windrr.jibrro.data.repository.datasource.SubwayArrivalRemoteDataSource
import retrofit2.Response

class SubwayArrivalRemoteDataSourceImpl(
    private val subwayApiService: SubwayApiService
) : SubwayArrivalRemoteDataSource {
    override suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Response<SubwayArrivalResponse> {
        return subwayApiService.getSubwayArrivalData(apiKey, statnNm = statnNm)
    }
}