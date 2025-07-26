package com.windrr.jibrro.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class StationViewModel @Inject constructor(
    private val getSubwayStationsUseCase: GetStationListUseCase
) : ViewModel() {

    private val _allStation = MutableStateFlow<List<SubwayStation>>(emptyList())
    val allStation: StateFlow<List<SubwayStation>> = _allStation.asStateFlow()

    private val _closestStation = MutableStateFlow<String?>(null)
    val closestStation: StateFlow<String?> = _closestStation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun findClosestStation(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            val stations = getSubwayStationsUseCase()

            Log.d("StationViewModel", "stations: $stations")
            val closest = stations.minByOrNull {
                distanceInMeters(lat, lng, it.lat, it.lng)
            }
            _closestStation.value = closest?.name
            _isLoading.value = false
        }
    }

    fun fetchStationList() {
        viewModelScope.launch {
            _isLoading.value = true
            val stations = getSubwayStationsUseCase()
            _allStation.value = stations
            _isLoading.value = false
        }
    }

    private fun distanceInMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}