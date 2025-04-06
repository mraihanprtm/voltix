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

    private val _loginState = MutableStateFlow<AuthResponse?>(null)
    val loginState: StateFlow<AuthResponse?> = _loginState

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            authManager.loginWithEmail(email, password)
                .collectLatest {
                    _loginState.value = it
                }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            authManager.signInWithGoogle()
                .collectLatest {
                    _loginState.value = it
                }
        }
    }
}