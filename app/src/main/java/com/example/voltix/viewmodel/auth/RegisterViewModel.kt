package com.example.voltix.viewmodel.auth // Sesuaikan package

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.response.AuthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    // Definisikan RegisterState seperti yang Anda gunakan di RegisterScreen
    sealed class RegisterState {
        data object Idle : RegisterState()
        data object Loading : RegisterState()
        data object Success : RegisterState() // Untuk menandakan registrasi + sinkronisasi backend berhasil
        data class Error(val message: String?) : RegisterState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    // SharedFlow untuk mengirim pesan/event sekali jalan ke UI (misalnya, untuk Snackbar)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    fun registerWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            authManager.registerWithEmail(email, password, name)
                .collectLatest { authResponse ->
                    when (authResponse) {
                        is AuthResponse.Success -> {
                            _registerState.value = RegisterState.Success
                            // Kirim event bahwa registrasi berhasil DAN email verifikasi telah (dicoba) dikirim.
                            // AuthManager sudah melakukan logging internal jika pengiriman email verifikasi gagal,
                            // tapi proses registrasi secara keseluruhan (akun dibuat & sync backend) tetap sukses.
                            _uiEvents.emit("Registrasi berhasil! Silakan cek email Anda untuk verifikasi.")
                        }
                        is AuthResponse.Error -> {
                            _registerState.value = RegisterState.Error(authResponse.message)
                            // Anda juga bisa mengirim pesan error melalui _uiEvents jika ingin semua pesan UI
                            // ditangani dengan cara yang sama (opsional, karena RegisterState.Error sudah ditangani di UI)
                            // _uiEvents.emit(authResponse.message ?: "Registrasi gagal.")
                        }
                        is AuthResponse.Loading -> {
                            _registerState.value = RegisterState.Loading // Tetap loading jika AuthManager mengirim state ini
                        }
                        else -> {
                            Log.w("RegisterViewModel", "Unexpected AuthResponse type: $authResponse")
                            _registerState.value = RegisterState.Error("Terjadi kesalahan tak terduga saat registrasi.")
                        }
                    }
                }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
}