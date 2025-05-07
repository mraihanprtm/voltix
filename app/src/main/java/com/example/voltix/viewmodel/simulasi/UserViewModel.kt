package com.example.voltix.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef
import com.example.voltix.data.relations.UserWithPerangkat
import com.example.voltix.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UserViewModel.kt
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    fun getCurrentUser() = liveData {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                emit(repository.getUserByUid(userId))
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getUserByUid(uid: String): UserEntity? {
        return try {
            repository.getUserByUid(uid)
        } catch (e: Exception) {
            null
        }
    }

    fun insertUser(user: UserEntity) = viewModelScope.launch {
        try {
            repository.insertUser(user)
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun updateUser(user: UserEntity) = viewModelScope.launch {
        try {
            repository.updateUser(user)
        } catch (e: Exception) {
            // Handle error
        }
    }
}
