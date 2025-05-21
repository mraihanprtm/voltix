package com.example.voltix.viewmodel.simulasi

import androidx.lifecycle.ViewModel
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity
import com.example.voltix.data.repository.ListrikRepository
import com.example.voltix.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GolonganListrikViewModel @Inject constructor(
    private val repository: ListrikRepository
) : ViewModel() {
    suspend fun getAllGolonganListrikwithBiaya(): List<GolonganListrikDenganBiaya>{
        println("Getting All Golongan Listrik with Biaya..")
        return repository.getAllGolonganListrikwithBiaya()
    }

    suspend fun getAllGolonganListrik(): List<GolonganListrikEntity>{
        return repository.getAllGolonganListrik()
    }
}