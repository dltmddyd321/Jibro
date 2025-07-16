package com.windrr.jibrro.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.windrr.jibrro.data.model.CheckStation
import kotlinx.coroutines.flow.Flow

@Dao
interface SubwayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: CheckStation)

    @Query("SELECT * FROM check_station")
    fun getAllCheckStation(): Flow<List<CheckStation>>

    @Query("DELETE FROM check_station WHERE id = :id")
    suspend fun deleteCheckStationById(id: String)
}