package com.example.voltix.data.remote.response

// ApiResponse.kt
data class ApiResponse<T>(
    val status: String,
    val data: T?,
    val message: String?
)
