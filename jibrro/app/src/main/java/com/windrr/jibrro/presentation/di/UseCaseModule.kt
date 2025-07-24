package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.domain.repository.CheckStationRepository
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.domain.repository.SubwayRepository
import com.windrr.jibrro.domain.usecase.DeleteStationUseCase
import com.windrr.jibrro.domain.usecase.GetCheckStationListUseCase
import com.windrr.jibrro.domain.usecase.GetStationListUseCase
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
import com.windrr.jibrro.domain.usecase.SaveStationListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Singleton
    @Provides
    fun provideGetSubwayArrivalUseCase(subwayRepository: SubwayRepository): GetSubwayArrivalDataUseCase {
        return GetSubwayArrivalDataUseCase(subwayRepository)
    }
    @Singleton
    @Provides
    fun provideGetCheckStationUseCase(checkStationRepository: CheckStationRepository): GetCheckStationListUseCase {
        return GetCheckStationListUseCase(checkStationRepository)
    }
    @Singleton
    @Provides
    fun provideDeleteSubwayArrivalUseCase(checkStationRepository: CheckStationRepository): DeleteStationUseCase {
        return DeleteStationUseCase(checkStationRepository)
    }
    @Singleton
    @Provides
    fun provideSaveStationListUseCase(checkStationRepository: CheckStationRepository): SaveStationListUseCase {
        return SaveStationListUseCase(checkStationRepository)
    }
    @Singleton
    @Provides
    fun provideGetStationListUseCase(repository: StationRepository): GetStationListUseCase {
        return GetStationListUseCase(repository)
    }
}