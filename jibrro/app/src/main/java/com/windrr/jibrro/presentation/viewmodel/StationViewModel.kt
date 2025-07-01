package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _stations = MutableLiveData<List<SubwayStation>>()
    val stations: LiveData<List<SubwayStation>> = _stations

    init {
        loadStations()
    }

    private fun loadStations() {
        _stations.value = getSubwayStationsUseCase()
    }

    fun findClosestStation(currentLat: Double, currentLon: Double): SubwayStation? {
        return _stations.value?.minByOrNull {
            distanceInMeters(currentLat, currentLon, it.lat, it.lng)
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