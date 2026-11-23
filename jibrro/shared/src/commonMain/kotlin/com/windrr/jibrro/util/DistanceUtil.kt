package com.windrr.jibrro.util

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun distanceInMeters(
    lat1: Double, lon1: Double, lat2: Double, lon2: Double
): Double {
    val earthRadiusMeters = 6_371_000.0
    val radiansPerDegree = PI / 180.0
    val dLat = (lat2 - lat1) * radiansPerDegree
    val dLon = (lon2 - lon1) * radiansPerDegree
    val a =
        sin(dLat / 2).pow(2.0) + cos(lat1 * radiansPerDegree) * cos(lat2 * radiansPerDegree) * sin(
            dLon / 2
        ).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusMeters * c
}
