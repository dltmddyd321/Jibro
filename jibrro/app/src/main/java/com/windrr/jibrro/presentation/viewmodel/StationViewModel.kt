package com.windrr.jibrro.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.usecase.GetClosestStationUseCase
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.StationSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val getSubwayStationsUseCase: GetStationListUseCase,
    private val getClosestStationUseCase: GetClosestStationUseCase,
    private val stationSearchUseCase: StationSearchUseCase
) : ViewModel() {

    private val _closestStation = MutableStateFlow<String?>(null)
    val closestStation: StateFlow<String?> = _closestStation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _stationList = MutableStateFlow<List<SubwayStation>>(emptyList())
    val stationList: StateFlow<List<SubwayStation>> = _stationList.asStateFlow()

    init {
        _stationList.value = getSubwayStationsUseCase()
    }

    fun findStationByName(name: String) {
        _stationList.value = stationSearchUseCase.searchByKeyword(name)
    }

    fun findClosestStation(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val closestStation = getClosestStationUseCase(lat, lng)
                _closestStation.value = closestStation?.name
                Log.d("StationViewModel", "Closest station: ${closestStation?.name}")
            } catch (e: Exception) {
                _closestStation.value = null
                Log.e("StationViewModel", "Error finding closest station", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}