package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.domain.repository.SubwayRepository
import com.windrr.jibrro.domain.usecase.GetSubwayArrivalDataUseCase
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
}