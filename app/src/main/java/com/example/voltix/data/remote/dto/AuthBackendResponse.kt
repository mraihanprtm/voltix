package com.example.voltix.data.remote.dto
import com.google.gson.annotations.SerializedName

data class AuthBackendResponse(
    val success: Boolean? = null,
    val message: String,
    @SerializedName("api_token")
    val apiToken: String? = null,
    val user: UserData // Pastikan ini mengacu ke UserData yang sudah diperbarui
)