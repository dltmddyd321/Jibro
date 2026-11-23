package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.data.model.Destination
import com.windrr.jibrro.domain.usecase.GetDestinationUseCase
import com.windrr.jibrro.domain.usecase.GetLastTrainNotificationUseCase
import com.windrr.jibrro.domain.usecase.SetDestinationUseCase
import com.windrr.jibrro.domain.usecase.SetLastLocationUseCase
import com.windrr.jibrro.domain.usecase.SetLastTrainNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLastTrainNotificationUseCase: GetLastTrainNotificationUseCase,
    private val setLastTrainNotificationUseCase: SetLastTrainNotificationUseCase,
    private val getDestinationUseCase: GetDestinationUseCase,
    private val setDestinationUseCase: SetDestinationUseCase,
    private val setLastLocationUseCase: SetLastLocationUseCase
) : ViewModel() {

    // getUseCase()가 반환하는 Flow<Boolean>을 UI 생명주기(viewModelScope)에
    // 따라 StateFlow로 변환하고, UI에서 자동 구독/해제되며, 기본값은 false
    val isLastTrainEnabled = getLastTrainNotificationUseCase().stateIn(
        viewModelScope,
        //구독자가 있을 때만 Flow를 active하게 유지
        //구독자가 사라지면 최대 5초간 유지 후 중단
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val destination = getDestinationUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun saveLastTrainEnabled(enabled: Boolean): Boolean = try {
        setLastTrainNotificationUseCase(enabled)
        true
    } catch (_: IOException) {
        false
    }

    suspend fun saveDestination(destination: Destination?): Boolean = try {
        setDestinationUseCase(destination)
        true
    } catch (_: IOException) {
        false
    }

    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            setLastLocationUseCase(lat, lng)
        }
    }
}