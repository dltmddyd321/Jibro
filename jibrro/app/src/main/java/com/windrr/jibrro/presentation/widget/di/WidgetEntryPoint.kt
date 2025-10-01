package com.windrr.jibrro.presentation.widget.di

import com.windrr.jibrro.domain.usecase.GetClosestStationUseCase
import com.windrr.jibrro.domain.usecase.GetLastLocationUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getSubwayArrivalDataUseCase(): GetSubwayArrivalDataUseCase
    fun getClosestStationUseCase(): GetClosestStationUseCase
    fun getLastLocationUseCase(): GetLastLocationUseCase
}
