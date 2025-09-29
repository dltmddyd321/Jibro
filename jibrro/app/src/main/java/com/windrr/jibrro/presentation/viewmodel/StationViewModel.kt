package com.windrr.jibrro.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.util.distanceInMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val getSubwayStationsUseCase: GetStationListUseCase
) : ViewModel() {

    private val _closestStation = MutableStateFlow<String?>(null)
    val closestStation: StateFlow<String?> = _closestStation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _stationList = MutableStateFlow<List<SubwayStation>>(emptyList())
    val stationList: StateFlow<List<SubwayStation>> = _stationList.asStateFlow()

    private val _allStations = getSubwayStationsUseCase().filter { it.lat != 0.0 && it.lng != 0.0 }

    init {
        _stationList.value = _allStations
    }

    fun findStationByName(name: String) {
        _stationList.value = if (name.isEmpty()) {
            _allStations
        } else {
            _allStations.filter { it.name in name }
        }
    }

    fun findClosestStation(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            val stations = getSubwayStationsUseCase().filter { it.lat != 0.0 && it.lng != 0.0 }

            Log.d("StationViewModel", "Valid stations count: ${stations.size}")
            Log.d("StationViewModel", "Finding closest station to: ($lat, $lng)")

            val closest = stations.minByOrNull {
                distanceInMeters(lat, lng, it.lat, it.lng)
            }
            Log.d("StationViewModel", "Closest station: ${closest?.name}")
            _closestStation.value = closest?.name
            _isLoading.value = false
        }
    }
}