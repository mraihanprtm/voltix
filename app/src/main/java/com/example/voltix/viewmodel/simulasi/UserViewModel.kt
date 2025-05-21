package com.example.voltix.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun getCurrentUser(): UserEntity? {
        println("Getting current User..")
        return try {
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    println("Current User = $user")
                } else {
                    println("No User is logged in")
                }
                user
            } else {
                println("FAILED TO GET CURRENT USER: ${result.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            println("Exception saat mendapatkan current user: ${e.message}")
            null
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
            Log.e("UserViewModel", "Error inserting user: ${e.message}")
        }
    }

    fun updateUser(user: UserEntity) = viewModelScope.launch {
        try {
            repository.updateUser(user)
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating user: ${e.message}")
        }
    }

    suspend fun getUserGolonganListrik(userid: Int): Int {
        try {
            return repository.getUserGolonganListrik(userid)
        } catch (e: Exception) {
            println("ERROR GETTING USER GOLONGAN LISTRIK")
            return 0
        }
    }

    suspend fun getUserisPrabayar(userid: Int): Boolean {
        try {
            return repository.getUserisPrabayar(userid)
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getUserBiayaListrik(userid: Int): List<GolonganListrikDenganBiaya> {
        return try {
            repository.getUserBiayaListrik(getUserGolonganListrik(userid))
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserTarif(userid: Int, usedKWH: Double): Int {
        return repository.getUserTarif(userid, usedKWH)
    }

    suspend fun getAllGolonganListrik(): List<GolonganListrikDenganBiaya> {
        return try {
            val result = repository.getAllGolonganListrik()
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                Log.d("UserViewModel", "Fetched all golongan listrik: $list")
                list
            } else {
                Log.e("UserViewModel", "Error fetching all golongan listrik: ${result.exceptionOrNull()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching all golongan listrik: ${e.message}")
            emptyList()
        }
    }
}