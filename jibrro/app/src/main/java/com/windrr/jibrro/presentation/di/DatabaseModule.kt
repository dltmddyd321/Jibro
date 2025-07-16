package com.windrr.jibrro.presentation.di

import android.app.Application
import androidx.room.Room
import com.windrr.jibrro.data.db.SubwayDao
import com.windrr.jibrro.data.db.SubwayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Singleton
    @Provides
    fun provideSubwayDatabase(app: Application): SubwayDatabase {
        return Room.databaseBuilder(app, SubwayDatabase::class.java, "news_db")
            .fallbackToDestructiveMigration(false)
            .build()
    }
    @Singleton
    @Provides
    fun provideSubwayDao(articleDatabase: SubwayDatabase): SubwayDao {
        return articleDatabase.subwayDao()
    }
}