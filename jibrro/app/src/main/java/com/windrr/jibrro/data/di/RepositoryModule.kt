package com.windrr.jibrro.data.di

import com.windrr.jibrro.data.repository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.data.repository.datasource.SubwayLocalDataSource
import com.windrr.jibrro.data.repository.repositoryImpl.AlarmRepositoryImpl
import com.windrr.jibrro.data.repository.repositoryImpl.CheckStationRepositoryImpl
import com.windrr.jibrro.data.repository.repositoryImpl.SettingsRepositoryImpl
import com.windrr.jibrro.data.repository.repositoryImpl.SubwayRepositoryImpl
import com.windrr.jibrro.domain.repository.AlarmRepository
import com.windrr.jibrro.domain.repository.CheckStationRepository
import com.windrr.jibrro.domain.repository.SettingsRepository
import com.windrr.jibrro.domain.repository.SubwayRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Singleton
    @Provides
    fun provideSubwayRepositoryRepository(
        subwayArrivalRemoteDataSource: SubwayArrivalRemoteDataSource,
    ): SubwayRepository {
        return SubwayRepositoryImpl(subwayArrivalRemoteDataSource)
    }
    @Singleton
    @Provides
    fun provideCheckStationRepository(
        subwayLocalDataSource: SubwayLocalDataSource
    ): CheckStationRepository {
        return CheckStationRepositoryImpl(subwayLocalDataSource)
    }
    @Singleton
    @Provides
    fun provideAlarmRepository(
        alarmRepositoryImpl: AlarmRepositoryImpl
    ): AlarmRepository {
        return alarmRepositoryImpl
    }
    @Provides
    @Singleton
    fun provideSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository = impl
}