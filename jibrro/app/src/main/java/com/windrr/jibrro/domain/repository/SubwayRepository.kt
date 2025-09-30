package com.windrr.jibrro.domain.repository

import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.state.Result

interface SubwayRepository {
    suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Result<List<RealtimeArrival>>
}