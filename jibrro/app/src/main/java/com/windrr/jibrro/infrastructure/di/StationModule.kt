package com.windrr.jibrro.infrastructure.di

import android.content.Context
import com.windrr.jibrro.domain.repository.StationRepository
import com.windrr.jibrro.data.repository.repositoryImpl.SubwayStationRepositoryImpl
import com.windrr.jibrro.data.repository.datasource.StationAssetDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideStationAssetDataSource(@ApplicationContext context: Context): StationAssetDataSource {
        return StationAssetDataSource(context)
    }

    @Provides
    fun provideSubwayStationRepository(
        dataSource: StationAssetDataSource
    ): StationRepository {
        return SubwayStationRepositoryImpl(dataSource)
    }
}