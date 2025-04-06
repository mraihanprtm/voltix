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
class RegisterViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _registerState = MutableStateFlow<AuthResponse?>(null)
    val registerState: StateFlow<AuthResponse?> = _registerState

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            authManager.createAccountWithEmail(email, password)
                .collectLatest {
                    _registerState.value = it
                }
        }
    }
}