package com.example.voltix.data.repository

import com.example.voltix.data.dao.GolonganListrikDao
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListrikRepository @Inject constructor(
    private val golonganListrikDao: GolonganListrikDao
){
    suspend fun getAllGolonganListrikwithBiaya(): List<GolonganListrikDenganBiaya>{
        return golonganListrikDao.getAllGolonganListrikwithBiaya()
    }

    suspend fun getAllGolonganListrik(): List<GolonganListrikEntity>{
        return golonganListrikDao.getAllGolonganListrik()
    }
}