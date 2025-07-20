package com.windrr.jibrro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.jibrro.domain.usecase.RegisterAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val registerAlarmUseCase: RegisterAlarmUseCase
) : ViewModel() {
    fun registerAlarm(timeMillis: Long) {
        viewModelScope.launch {
            registerAlarmUseCase(timeMillis)
        }
    }
}
