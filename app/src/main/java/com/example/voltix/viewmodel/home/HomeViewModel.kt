package com.example.voltix.viewmodel.home

import android.util.Log // Tambahkan untuk Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Tambahkan ini jika belum ada
import kotlinx.coroutines.flow.collectLatest // Tambahkan ini
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow() // Gunakan asStateFlow()

    private val _userUid = MutableStateFlow<String?>(null)
    val userUid: StateFlow<String?> = _userUid.asStateFlow() // Gunakan asStateFlow()

    init {
        viewModelScope.launch {
            // Observasi StateFlow currentUserProfile dari UserRepository
            userRepository.currentUserProfile.collectLatest { userData ->
                if (userData != null) {
                    // Jika UserData ada, update UID dan nama
                    _userUid.value = userData.firebaseUid
                    _userName.value = userData.name
                    Log.d("HomeViewModel", "User profile loaded: Name='${userData.name}', UID='${userData.firebaseUid}'")
                } else {
                    // Jika UserData null (misal, pengguna belum login atau data belum termuat)
                    _userUid.value = null
                    _userName.value = null
                    Log.d("HomeViewModel", "User profile is null.")
                    // Opsional: Jika Anda ingin memicu pengambilan data jika null dan ada token
                    // Ini berguna jika HomeViewModel adalah layar pertama setelah splash dan user sudah login sebelumnya
                    // if (userRepository.tokenManager.getApiToken() != null) {
                    //     Log.d("HomeViewModel", "Token exists, attempting to fetch user profile.")
                    //     userRepository.fetchAndSetCurrentUserProfileFromBackend()
                    // }
                }
            }
        }
        // Pemicu awal untuk fetch user jika belum ada dan token sudah ada (misalnya saat app baru dibuka)
        // Ini bisa juga dilakukan oleh sebuah MainViewModel atau logic di Activity/Composable utama.
        if (userRepository.currentUserProfile.value == null && userRepository.tokenManager.getApiToken() != null) {
            viewModelScope.launch { userRepository.fetchAndSetCurrentUserProfileFromBackend() }
        }
    }
}