package com.windrr.jibrro.domain.state

//Null 보다는 명시적인 State 처리가 좋을듯
sealed class LocationState {
    data object Empty : LocationState()
    data class Available(val lat: Double, val lng: Double) : LocationState()
}