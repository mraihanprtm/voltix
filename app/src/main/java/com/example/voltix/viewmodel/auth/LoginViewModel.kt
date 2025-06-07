package com.example.voltix.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.response.AuthResponse
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * LoginViewModel bertanggung jawab untuk mengelola state UI dan logika bisnis terkait login.
 * Ini berinteraksi dengan AuthManager untuk melakukan operasi autentikasi.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    /**
     * Sealed class untuk merepresentasikan state dari proses login.
     */
    sealed class LoginState {
        data object Idle : LoginState() // Kondisi awal atau setelah reset
        data object Loading : LoginState() // Proses login sedang berjalan
        data object Success : LoginState() // Login berhasil
        data class Error(val message: String?) : LoginState() // Terjadi error saat login
    }

    // StateFlow yang diekspos ke UI untuk mengamati status login
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // SharedFlow untuk mengirim pesan/event sekali jalan ke UI (misalnya, untuk Snackbar)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "loginWithEmail: Attempting to login with email: $email")
            authManager.loginWithEmail(email, password)
                .collectLatest { authResponse ->
                    Log.d("LoginViewModel", "loginWithEmail: AuthResponse from AuthManager: $authResponse")
                    _loginState.value = when (authResponse) {
                        is AuthResponse.Success -> {
                            val user = FirebaseAuth.getInstance().currentUser
                            val isNewUser = user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp
                            Log.d("LoginViewModel", "loginWithEmail: isNewUser=$isNewUser")
                            LoginState.Success
                        }
                        is AuthResponse.Error -> LoginState.Error(authResponse.message)
                        is AuthResponse.Loading -> LoginState.Loading
                        else -> {
                            Log.w("LoginViewModel", "loginWithEmail: Unexpected AuthResponse type: $authResponse")
                            LoginState.Error("Terjadi kesalahan tak terduga saat login.")
                        }
                    }
                }
        }
    }

    fun processGoogleSignInToken(idTokenGoogle: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("LoginViewModel", "processGoogleSignInToken: Processing Google idToken...")
            authManager.signInWithGoogleCredential(idTokenGoogle)
                .collectLatest { authResponse ->
                    Log.d("LoginViewModel", "processGoogleSignInToken: AuthResponse from AuthManager: $authResponse")
                    _loginState.value = when (authResponse) {
                        is AuthResponse.Success -> {
                            val user = FirebaseAuth.getInstance().currentUser
                            val isNewUser = user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp
                            Log.d("LoginViewModel", "processGoogleSignInToken: isNewUser=$isNewUser")
                            LoginState.Success
                        }
                        is AuthResponse.Error -> LoginState.Error(authResponse.message)
                        is AuthResponse.Loading -> LoginState.Loading
                        else -> {
                            Log.w("LoginViewModel", "processGoogleSignInToken: Unexpected AuthResponse type: $authResponse")
                            LoginState.Error("Terjadi kesalahan tak terduga.")
                        }
                    }
                }
        }
    }

    /**
     * Mengirim email reset password.
     *
     * @param email Email tujuan pengiriman link reset.
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            // Tidak perlu mengubah _loginState ke Loading di sini,
            // karena ini bukan proses login utama.
            authManager.sendPasswordResetEmail(email)
                .collectLatest { authResponse ->
                    when (authResponse) {
                        is AuthResponse.Success -> {
                            _uiEvents.emit("Email reset password telah dikirim ke $email.")
                            // Pastikan _loginState kembali ke Idle jika sebelumnya Loading
                            if (_loginState.value is LoginState.Loading) {
                                _loginState.value = LoginState.Idle
                            }
                        }
                        is AuthResponse.Error -> {
                            _uiEvents.emit(authResponse.message ?: "Gagal mengirim email reset.")
                            if (_loginState.value is LoginState.Loading) {
                                _loginState.value = LoginState.Error(authResponse.message) // Atau Idle
                            }
                        }
                        is AuthResponse.Loading -> {
                            // Abaikan jika tidak ada UI loading spesifik untuk ini
                        }
                        else -> {
                            Log.w("LoginViewModel", "sendPasswordResetEmail: Unexpected AuthResponse type: $authResponse")
                            _uiEvents.emit("Terjadi kesalahan tak terduga saat mengirim email reset.")
                        }
                    }
                    Log.d("LoginViewModel", "sendPasswordResetEmail: LoginState after reset attempt: ${_loginState.value}")
                }
        }
    }

    /**
     * Mereset state login ke Idle.
     * Dipanggil setelah UI selesai menangani Success/Error.
     */
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
        Log.d("LoginViewModel", "resetLoginState: LoginState reset to Idle.")
    }
}
