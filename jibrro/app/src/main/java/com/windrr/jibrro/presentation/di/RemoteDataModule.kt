package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.data.api.SubwayApiService
import com.windrr.jibrro.data.respository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.data.respository.datasourceImpl.SubwayArrivalRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RemoteDataModule {
    @Singleton
    @Provides
    fun provideSubwayArrivalDataSource(
        subwayApiService: SubwayApiService
    ): SubwayArrivalRemoteDataSource {
        return SubwayArrivalRemoteDataSourceImpl(subwayApiService)
    }
}