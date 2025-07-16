package com.windrr.jibrro.presentation.di

import com.windrr.jibrro.data.db.SubwayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {
//    @Singleton
//    @Provides
//    fun provideLocalDataSource(subwayDao: SubwayDao): NewsLocalDataSource {
//        return NewsLocalDataSourceImpl(subwayDao)
//    }
}