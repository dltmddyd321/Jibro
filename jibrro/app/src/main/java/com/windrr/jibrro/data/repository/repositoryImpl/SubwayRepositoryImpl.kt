package com.windrr.jibrro.data.repository.repositoryImpl

import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.data.model.ErrorMessage
import com.windrr.jibrro.data.model.SubwayArrivalResponse
import com.windrr.jibrro.data.repository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.domain.state.Result
import com.windrr.jibrro.domain.repository.SubwayRepository
import retrofit2.Response

class SubwayRepositoryImpl(
    private val newsRemoteDataSource: SubwayArrivalRemoteDataSource
): SubwayRepository {
    private fun convertToSubwayArrivalData(response: Response<SubwayArrivalResponse>): Result<List<RealtimeArrival>> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                val arrivals: List<RealtimeArrival>? = result.realtimeArrivalList
                if (arrivals != null) return Result.Success(arrivals)
                val error: ErrorMessage? = result.errorMessage
                if (error?.code == "INFO-000" || error?.code == "INFO-200") {
                    return Result.Success(emptyList())
                }
            }
        }
        return Result.Error("지하철 도착 정보를 불러오지 못했어요.")
    }

    override suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Result<List<RealtimeArrival>> {
        return convertToSubwayArrivalData(newsRemoteDataSource.getSubwayArrivalData(apiKey, statnNm))
    }
}
