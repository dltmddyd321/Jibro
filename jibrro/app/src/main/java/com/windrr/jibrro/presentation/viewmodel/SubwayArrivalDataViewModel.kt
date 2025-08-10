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

    private val _arrivalMap =
        MutableStateFlow<Map<String, Result<List<RealtimeArrival>>>>(emptyMap())
    val arrivalMap: StateFlow<Map<String, Result<List<RealtimeArrival>>>> = _arrivalMap

    private fun getSubwayArrivalFor(statnNm: String) {
        viewModelScope.launch {
            _arrivalMap.value = _arrivalMap.value.toMutableMap().apply {
                this[statnNm] = Result.Loading()
            }

            val result = getSubwayArrivalDataUseCase.execute(statnNm = statnNm)

            _arrivalMap.value = _arrivalMap.value.toMutableMap().apply {
                this[statnNm] = result
            }
        }
    }

    fun refreshStations(stations: List<String>) {
        stations.distinct().forEach { getSubwayArrivalFor(it) }
    }
}