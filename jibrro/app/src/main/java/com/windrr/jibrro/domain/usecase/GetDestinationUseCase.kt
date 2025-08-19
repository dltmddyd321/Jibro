package com.windrr.jibrro.domain.usecase

import com.windrr.jibrro.domain.repository.DataStoreRepository

class GetDestinationUseCase(private val repository: DataStoreRepository) {
    suspend operator fun invoke(key: String): String? = repository.getString(key)
}
