package com.windrr.jibrro.data.model

data class SubwayArrivalResponse(
    val errorMessage: ErrorMessage,
    val realtimeArrivalList: List<RealtimeArrival>
)