package com.example.voltix.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
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

    suspend fun getUserGolonganListrik(userid: Int): Int{
        try {
            return repository.getUserGolonganListrik(userid)
        } catch (e: Exception) {
            println("ERROR GETTING USER GOLONGAN LISTRIK")
            return 0
        }
    }

    suspend fun getUserisPrabayar(userid: Int): Boolean{
        try {
            return repository.getUserisPrabayar(userid)
        } catch (e: Exception){
            return false
        }
    }

    suspend fun getUserBiayaListrik(userid: Int): List<GolonganListrikDenganBiaya>{
        return try {
            repository.getUserBiayaListrik(getUserGolonganListrik(userid))
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun getUserTarif(userid: Int, usedKWH: Double): Int{
        var isPrabayar = getUserisPrabayar(userid)
        var biayaListrik = repository.getUserBiayaListrik(userid)
        biayaListrik = biayaListrik.reversed()
        val golonganTarif = biayaListrik.firstOrNull { usedKWH > it.minKWH }
        if (isPrabayar){
            Log.d("DEBUG", "Biaya Tarif Listrik = ${golonganTarif!!.biayaPrabayar}")
            return golonganTarif.biayaPrabayar
        } else {
            Log.d("DEBUG", "Biaya Tarif Listrik = ${golonganTarif!!.biayaPrabayar}")
            return golonganTarif.biayaReguler
        }
    }


}
