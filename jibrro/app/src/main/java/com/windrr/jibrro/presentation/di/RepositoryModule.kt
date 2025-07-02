package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.data.respository.datasource.SubwayArrivalRemoteDataSource
import com.windrr.jibrro.data.respository.repositoryImpl.SubwayRepositoryImpl
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
    fun provideNewsRepository(
        subwayArrivalRemoteDataSource: SubwayArrivalRemoteDataSource,
    ): SubwayRepository {
        return SubwayRepositoryImpl(subwayArrivalRemoteDataSource)
    }
}