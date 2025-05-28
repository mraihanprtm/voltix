// com.example.voltix.data.remote.api.AuthApiService.kt
package com.example.voltix.data.remote.api

import com.example.voltix.data.remote.dto.AuthBackendResponse
import com.example.voltix.data.remote.dto.FirebaseLoginRequest
import com.example.voltix.data.remote.dto.ProfileUpdateRequest
import com.example.voltix.data.remote.dto.UserData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("api/v1/auth/firebase-login-or-register") // Sesuaikan dengan URL lengkap endpoint Laravel Anda
    suspend fun loginOrRegisterWithFirebaseToken(
        @Body payload: FirebaseLoginRequest
    ): Response<AuthBackendResponse>

    @GET("api/v1/user")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") token: String
    ): Response<UserData>

    @PUT("api/v1/user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body payload: ProfileUpdateRequest
    ): Response<AuthBackendResponse>
}