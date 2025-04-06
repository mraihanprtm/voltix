package com.example.voltix.data.remote.response

interface AuthResponse {
    data object Success : AuthResponse
    data class Error (val message: String) : AuthResponse
}