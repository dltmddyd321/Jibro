package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.infrastructure.LastTrainAlert
import java.io.IOException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.windrr.jibrro.domain.state.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
internal class SubwayArrivalDataViewModel @Inject constructor(
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase,
    private val lastTrainAlert: LastTrainAlert
) : ViewModel() {

    private val _arrivalMap =
        MutableStateFlow<Map<String, Result<List<RealtimeArrival>>>>(emptyMap())
    val arrivalMap: StateFlow<Map<String, Result<List<RealtimeArrival>>>> = _arrivalMap
    private val requests = mutableMapOf<String, Job>()

    private fun getSubwayArrivalFor(statnNm: String, checkLastTrain: Boolean) {
        if (requests[statnNm]?.isActive == true) return
        requests[statnNm] = viewModelScope.launch {
            val cached = _arrivalMap.value[statnNm]
            if (cached !is Result.Success || cached.data.isNullOrEmpty()) {
                _arrivalMap.value += statnNm to Result.Loading()
            }
            val result = try {
                val query = if (statnNm == "서울역") "서울" else statnNm
                getSubwayArrivalDataUseCase.execute(statnNm = query)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                Result.Error("지하철 도착 정보를 불러오지 못했어요.")
            }
            _arrivalMap.value += statnNm to result
            if (checkLastTrain && result is Result.Success) {
                try {
                    lastTrainAlert.check(statnNm, result.data.orEmpty())
                } catch (_: IOException) {
                    // The arrival list remains available if alert settings cannot be read.
                }
            }
        }
    }

    fun refreshStations(stations: List<String>, nearestStation: String? = null) {
        stations.distinct().forEach { getSubwayArrivalFor(it, checkLastTrain = it == nearestStation) }
    }
}
