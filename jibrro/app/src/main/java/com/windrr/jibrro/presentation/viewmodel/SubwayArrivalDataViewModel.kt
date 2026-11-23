package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.windrr.jibrro.domain.state.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class SubwayArrivalDataViewModel @Inject constructor(
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase
) : ViewModel() {

    private val _arrivalMap =
        MutableStateFlow<Map<String, Result<List<RealtimeArrival>>>>(emptyMap())
    val arrivalMap: StateFlow<Map<String, Result<List<RealtimeArrival>>>> = _arrivalMap
    private val requests = mutableMapOf<String, Job>()

    private fun getSubwayArrivalFor(statnNm: String) {
        if (requests[statnNm]?.isActive == true) return
        requests[statnNm] = viewModelScope.launch {
            if (statnNm !in _arrivalMap.value) {
                _arrivalMap.value += statnNm to Result.Loading()
            }
            val result = try {
                getSubwayArrivalDataUseCase.execute(statnNm = statnNm)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                Result.Error("지하철 도착 정보를 불러오지 못했어요.")
            }
            _arrivalMap.value += statnNm to result
        }
    }

    fun refreshStations(stations: List<String>) {
        stations.distinct().forEach { getSubwayArrivalFor(it) }
    }
}
