package com.windrr.jibrro.presentation.widget.di

import com.windrr.jibrro.domain.usecase.GetLastLocationUseCase
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getSubwayArrivalDataUseCase(): GetSubwayArrivalDataUseCase
    fun getStationListUseCase(): GetStationListUseCase
    fun getLastLocationUseCase(): GetLastLocationUseCase
}
