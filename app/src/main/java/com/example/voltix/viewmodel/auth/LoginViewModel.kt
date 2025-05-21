package com.example.voltix.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.remote.AuthManager
import com.example.voltix.data.remote.response.AuthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String?) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authManager.loginWithEmail(email, password)
                .collectLatest { authResponse ->
                    _loginState.value = when (authResponse) {
                        is AuthResponse.Success -> LoginState.Success
                        is AuthResponse.Error -> LoginState.Error(authResponse.message)
                        else -> LoginState.Error("Unexpected response")
                    }
                }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authManager.signInWithGoogle()
                .collectLatest { authResponse ->
                    _loginState.value = when (authResponse) {
                        is AuthResponse.Success -> LoginState.Success
                        is AuthResponse.Error -> LoginState.Error(authResponse.message)
                        else -> LoginState.Error("Unexpected response")
                    }
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authManager.sendPasswordResetEmail(email)
                .collectLatest { authResponse ->
                    _loginState.value = when (authResponse) {
                        is AuthResponse.Success -> LoginState.Success
                        is AuthResponse.Error -> LoginState.Error(authResponse.message)
                        else -> LoginState.Error("Unexpected response")
                    }
                }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}