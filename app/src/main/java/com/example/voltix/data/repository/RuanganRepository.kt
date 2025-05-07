package com.example.voltix.data.repository

import androidx.lifecycle.LiveData
import com.example.voltix.data.dao.RuanganDAO
import com.example.voltix.data.entity.RuanganEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuanganRepository @Inject constructor(
    private val ruanganDao: RuanganDAO
) {
    // LiveData untuk observasi perubahan data
    val allRuangan: LiveData<List<RuanganEntity>> = ruanganDao.getAllRuangan()

    suspend fun insertRuangan(ruangan: RuanganEntity) {
        ruanganDao.insertRuangan(ruangan)
    }

    suspend fun deleteRuangan(ruangan: RuanganEntity) {
        ruanganDao.deleteRuangan(ruangan)
    }

    suspend fun updateRuangan(ruangan: RuanganEntity) {
        ruanganDao.updateRuangan(ruangan)
    }

    fun getNamaRuangan(ruanganId: Int): Flow<String?> {
        return ruanganDao.getNamaRuangan(ruanganId)
    }

    // Fungsi suspend untuk mendapatkan list langsung
    suspend fun getAllRuangan(): List<RuanganEntity> {
        return ruanganDao.getAllRuanganList()
    }
}