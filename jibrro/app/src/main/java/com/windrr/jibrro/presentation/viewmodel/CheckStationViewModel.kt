package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.CheckStation
import com.windrr.jibrro.domain.usecase.DeleteStationUseCase
import com.windrr.jibrro.domain.usecase.GetCheckStationListUseCase
import com.windrr.jibrro.domain.usecase.SaveStationListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckStationViewModel @Inject constructor(
    private val deleteStationUseCase: DeleteStationUseCase,
    private val getCheckStationUseCase: GetCheckStationListUseCase,
    private val saveStationListUseCase: SaveStationListUseCase
) : ViewModel() {
    private val _checkStationList = MutableStateFlow<List<CheckStation>>(emptyList())
    val checkStationList = _checkStationList

    fun fetchCheckedStationList() {
        viewModelScope.launch {
            getCheckStationUseCase().collect { stations ->
                _checkStationList.value = stations
            }
        }
    }

    fun saveCheckedStationList(stations: List<CheckStation>) {
        viewModelScope.launch {
            saveStationListUseCase(stations)
        }
    }

    fun deleteCheckedStationList(id: String) {
        viewModelScope.launch {
            deleteStationUseCase(id)
        }
    }
}