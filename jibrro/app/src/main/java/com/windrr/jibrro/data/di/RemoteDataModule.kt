package com.windrr.jibrro.data.di

import com.windrr.jibrro.data.api.SubwayApiService
import com.windrr.jibrro.data.repository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.data.repository.datasourceImpl.SubwayArrivalRemoteDataSourceImpl
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