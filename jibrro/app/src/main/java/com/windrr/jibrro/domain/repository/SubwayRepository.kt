package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.SubwayArrival
import com.windrr.jibrro.data.util.Result

interface SubwayRepository {
    suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Result<List<SubwayArrival>>
}