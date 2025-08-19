package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.DataStoreRepository

class SaveDestinationUseCase(private val repository: DataStoreRepository) {
    suspend operator fun invoke(key: String, value: String) = repository.saveString(key, value)
}
