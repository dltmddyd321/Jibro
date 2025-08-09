package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.RealtimeArrival
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.windrr.jibrro.data.util.Result
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SubwayArrivalDataViewModel @Inject constructor(
    private val getSubwayArrivalDataUseCase: GetSubwayArrivalDataUseCase
) : ViewModel() {
    private val _arrivalState = MutableStateFlow<Result<List<RealtimeArrival>>>(Result.Loading())
    val arrivalState: StateFlow<Result<List<RealtimeArrival>>> = _arrivalState.asStateFlow()

    private val _arrivalMap = MutableStateFlow<Map<String, Result<List<RealtimeArrival>>>>(emptyMap())
    val arrivalMap: StateFlow<Map<String, Result<List<RealtimeArrival>>>> = _arrivalMap

    fun getSubwayArrivalFor(statnNm: String) {
        viewModelScope.launch {
            // 1) 로딩 표시
            _arrivalMap.value = _arrivalMap.value.toMutableMap().apply {
                this[statnNm] = Result.Loading()
            }

            // 2) 실제 호출
            val result = getSubwayArrivalDataUseCase.execute(statnNm = statnNm)

            // 3) 결과 반영
            _arrivalMap.value = _arrivalMap.value.toMutableMap().apply {
                this[statnNm] = result
            }
        }
    }

    fun refreshStations(stations: List<String>) {
        stations.distinct().forEach { getSubwayArrivalFor(it) }
    }

    fun getSubwayArrival(statnNm: String) {
        viewModelScope.launch {
            _arrivalState.value = Result.Loading()
            val result = getSubwayArrivalDataUseCase.execute(statnNm = statnNm)
            _arrivalState.value = result
        }
    }
}