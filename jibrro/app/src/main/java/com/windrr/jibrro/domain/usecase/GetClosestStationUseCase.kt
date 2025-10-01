package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.data.model.SubwayStation
import com.windrr.jibrro.util.distanceInMeters
import javax.inject.Inject

class GetClosestStationUseCase @Inject constructor(
    private val getStationListUseCase: GetStationListUseCase
) {
    operator fun invoke(lat: Double, lng: Double): SubwayStation? {
        val stations = getStationListUseCase()
            .filter { it.lat != 0.0 && it.lng != 0.0 }

        return stations.minByOrNull { station ->
            distanceInMeters(lat, lng, station.lat, station.lng)
        }
    }
}
