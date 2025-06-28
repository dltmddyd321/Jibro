package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
}