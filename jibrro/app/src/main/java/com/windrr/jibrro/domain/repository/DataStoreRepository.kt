package com.windrr.jibrro.domain.repository

interface DataStoreRepository {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String): String?
}
