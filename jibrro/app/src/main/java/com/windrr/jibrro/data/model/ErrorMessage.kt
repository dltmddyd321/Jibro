package com.windrr.jibrro.data.model

data class ErrorMessage(
    val code: String,
    val developerMessage: String,
    val link: String,
    val message: String,
    val status: Int,
    val total: Int
)