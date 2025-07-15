package com.windrr.jibrro.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.windrr.jibrro.data.model.CheckStation

@Database(entities = [CheckStation::class], version = 1, exportSchema = false)
abstract class SubwayDatabase : RoomDatabase() {
    abstract fun subwayDao(): SubwayDao
}
