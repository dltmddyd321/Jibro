package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.BuildConfig
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.state.Result
import com.windrr.jibrro.domain.repository.SubwayRepository

class GetSubwayArrivalDataUseCase(private val subwayRepository: SubwayRepository) {
    suspend fun execute(apiKey: String = BuildConfig.SUBWAY_API_KEY, statnNm: String): Result<List<RealtimeArrival>> {
        if (apiKey.isBlank()) return Result.Error("지하철 API 인증키가 설정되어 있지 않습니다.")
        return subwayRepository.getSubwayArrivalData(apiKey, statnNm)
    }
}
