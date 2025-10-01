package com.windrr.jibrro.data.di

import android.content.Context
import com.windrr.jibrro.data.db.SubwayDao
import com.windrr.jibrro.data.repository.datasource.StationDataSource
import com.windrr.jibrro.data.repository.datasource.SubwayLocalDataSource
import com.windrr.jibrro.data.repository.datasourceImpl.StationDataSourceImpl
import com.windrr.jibrro.data.repository.datasourceImpl.SubwayLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {
    @Singleton
    @Provides
    fun provideLocalDataSource(subwayDao: SubwayDao): SubwayLocalDataSource {
        return SubwayLocalDataSourceImpl(subwayDao)
    }

    @Singleton
    @Provides
    fun provideStationDataSource(
        @ApplicationContext context: Context
    ): StationDataSource {
        return StationDataSourceImpl(context)
    }
}