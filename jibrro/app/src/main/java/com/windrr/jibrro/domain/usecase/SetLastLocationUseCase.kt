package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//Module 구성 따로 없이 처리
class SetLastLocationUseCase @Inject constructor(
    private val repo: SettingsRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        repo.setLastLat(lat)
        repo.setLastLng(lng)
    }
}