package com.example.voltix.data.util

import com.example.voltix.data.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor ini "menangkap" setiap permintaan keluar, menambahkan header
 * Authorization jika token tersedia, dan kemudian membiarkan permintaan melanjutkan.
 *
 * @param tokenManager Manajer token yang di-inject untuk mengakses token API Laravel.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Ambil permintaan asli
        val originalRequest = chain.request()

        // Ambil token dari manajer token Anda
        val token = tokenManager.getApiToken() // CHANGED: Menggunakan getApiToken()

        val requestBuilder = originalRequest.newBuilder()
            // Selalu tambahkan header ini untuk memastikan server merespons dengan JSON
            .header("Accept", "application/json")

        // Jika token ada, buat permintaan baru dengan header Authorization
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        // Lanjutkan dengan permintaan yang baru atau yang asli
        return chain.proceed(request)
    }
}
