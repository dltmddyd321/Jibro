package com.windrr.jibrro.data.repository.repositoryImpl

import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.data.model.SubwayArrivalResponse
import com.windrr.jibrro.data.repository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.data.util.Result
import com.windrr.jibrro.domain.repository.SubwayRepository
import retrofit2.Response

class SubwayRepositoryImpl(
    private val newsRemoteDataSource: SubwayArrivalRemoteDataSource
): SubwayRepository {
    private fun convertToSubwayArrivalData(response: Response<SubwayArrivalResponse>): Result<List<RealtimeArrival>> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                return Result.Success(result.realtimeArrivalList)
            }
        }
        return Result.Error(response.message())
    }

    override suspend fun getSubwayArrivalData(
        apiKey: String,
        statnNm: String
    ): Result<List<RealtimeArrival>> {
        return convertToSubwayArrivalData(newsRemoteDataSource.getSubwayArrivalData(apiKey, statnNm))
    }
}