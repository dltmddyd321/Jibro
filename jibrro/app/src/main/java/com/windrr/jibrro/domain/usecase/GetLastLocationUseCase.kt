package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository
import com.windrr.jibrro.domain.state.LocationState
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetLastLocationUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(): LocationState {
        val lat = repo.getLastLat().first()
        val lng = repo.getLastLng().first()
        return if (lat != null && lng != null) {
            LocationState.Available(lat, lng)
        } else {
            LocationState.Empty
        }
    }
}