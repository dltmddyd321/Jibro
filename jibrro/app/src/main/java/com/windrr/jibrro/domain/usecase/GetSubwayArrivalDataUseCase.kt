package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.BuildConfig
import com.windrr.jibrro.data.model.SubwayArrival
import com.windrr.jibrro.data.util.Result
import com.windrr.jibrro.domain.repository.SubwayRepository

class GetSubwayArrivalDataUseCase(private val subwayRepository: SubwayRepository) {
    suspend fun execute(apiKey: String = BuildConfig.SUBWAY_API_KEY, statnNm: String): Result<List<SubwayArrival>> {
        return subwayRepository.getSubwayArrivalData(apiKey, statnNm)
    }
}