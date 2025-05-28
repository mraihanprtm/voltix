package com.example.voltix.data.remote.response

import com.example.voltix.data.remote.dto.UserData // Pastikan UserData di-import

/**
 * Sealed interface untuk merepresentasikan status respons autentikasi.
 * Digunakan untuk mengkomunikasikan hasil operasi autentikasi (login, register, dll.)
 * dari AuthManager ke ViewModel.
 */
sealed interface AuthResponse {
    /**
     * Menunjukkan bahwa operasi sedang dalam proses loading.
     */
    data object Loading : AuthResponse

    /**
     * Menunjukkan bahwa operasi berhasil.
     * Dapat membawa data pengguna (UserData) jika operasi tersebut menghasilkan data pengguna.
     */
    data class Success(val userData: UserData? = null) : AuthResponse

    /**
     * Menunjukkan bahwa operasi mengalami error.
     * Membawa pesan error untuk ditampilkan kepada pengguna.
     */
    data class Error (val message: String) : AuthResponse
}
