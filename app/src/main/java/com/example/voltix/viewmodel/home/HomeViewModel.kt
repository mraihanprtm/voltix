package com.example.voltix.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userUid = MutableStateFlow<String?>(null)
    val userUid: StateFlow<String?> = _userUid

    init {
        viewModelScope.launch {
            // Ambil current UID dari FirebaseAuth lewat repository
            val firebaseUid = userRepository.auth.currentUser?.uid
            _userUid.value = firebaseUid

            // Jika ada UID, fetch UserEntity
            firebaseUid?.let { uid ->
                val user = userRepository.getUserByUid(uid)
                _userName.value = user?.name
            }
        }
    }
}