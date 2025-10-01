package com.windrr.jibrro.data.di

import android.content.Context
import com.windrr.jibrro.data.repository.datasource.SettingsLocalDataSource
import com.windrr.jibrro.data.repository.datasourceImpl.SettingsLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideSettingsLocalDataSource(
        @ApplicationContext context: Context
    ): SettingsLocalDataSource = SettingsLocalDataSourceImpl(context)
}