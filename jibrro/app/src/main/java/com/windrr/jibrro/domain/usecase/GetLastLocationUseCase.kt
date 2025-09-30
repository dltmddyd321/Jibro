package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository
import com.windrr.jibrro.domain.state.LocationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class GetLastLocationUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    operator fun invoke(): Flow<LocationState> =
        combine(repo.getLastLat(), repo.getLastLng()) { lat, lng ->
            when {
                lat != null && lng != null -> LocationState.Available(lat, lng)
                else -> LocationState.Empty
            }
        }.distinctUntilChanged()
}