package com.windrr.jibrro.domain.commute

data class Coordinates(val latitude: Double, val longitude: Double) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
            (latitude != 0.0 || longitude != 0.0)
}

data class HomeLocation(val label: String, val point: Coordinates)

enum class TransitMode(val label: String) {
    SUBWAY("지하철"),
    BUS("버스")
}

data class TransitLine(val name: String, val localId: String? = null, val cityCode: Int? = null)

data class CommuteLeg(
    val mode: TransitMode,
    val start: String,
    val end: String,
    val lines: List<TransitLine> = emptyList(),
    val direction: String? = null,
    val directionCode: Int? = null,
    val stationArsId: String? = null,
    val localStationId: String? = null,
    val stations: List<String> = emptyList()
)

data class CommuteRoute(
    val minutes: Int,
    val approachMinutes: Int,
    val rides: List<CommuteLeg>
) {
    val firstRide: CommuteLeg? get() = rides.firstOrNull()
    val transfers: Int get() = (rides.size - 1).coerceAtLeast(0)
}

data class BoardingArrival(
    val seconds: Int?,
    val stopsAway: Int?,
    val status: String? = null,
    val line: String? = null,
    val serviceEnded: Boolean = false,
    val direction: String? = null
)
