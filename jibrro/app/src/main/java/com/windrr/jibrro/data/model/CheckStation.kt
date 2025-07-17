package com.windrr.jibrro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_station")
data class CheckStation(
    @PrimaryKey val id: String,
    val name: String,
    val line: String
)