package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.data.db.SubwayDao
import com.windrr.jibrro.data.repository.datasource.SubwayLocalDataSource
import com.windrr.jibrro.data.repository.datasourceImpl.SubwayLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}