package com.example.voltix.repository

import com.example.voltix.data.dao.PerangkatDao
import com.example.voltix.data.PerangkatEntity
import com.example.voltix.data.entity.PerangkatListrikEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PerangkatRepository @Inject constructor(
    private val perangkatDao: PerangkatDao
) {
    val allPerangkat = perangkatDao.getAllPerangkat()

    suspend fun insert(perangkat: PerangkatListrikEntity) {
        withContext(Dispatchers.IO) {
            perangkatDao.insertPerangkat(perangkat)
        }
    }

    suspend fun update(perangkat: PerangkatListrikEntity) {
        withContext(Dispatchers.IO) {
            perangkatDao.updatePerangkat(perangkat)
        }
    }

    suspend fun delete(perangkat: PerangkatListrikEntity) {
        withContext(Dispatchers.IO) {
            perangkatDao.deletePerangkat(perangkat)
        }
    }

    suspend fun getById(id: Int): PerangkatListrikEntity? {
        return withContext(Dispatchers.IO) {
            perangkatDao.getPerangkatById(id)
        }
    }
}
