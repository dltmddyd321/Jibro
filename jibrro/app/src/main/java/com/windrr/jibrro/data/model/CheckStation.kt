package com.windrr.jibrro.data.model

import androidx.room.Entity

@Entity(tableName = "check_station")
data class CheckStation(
    val id: String,
    val name: String,
    val line: String
)