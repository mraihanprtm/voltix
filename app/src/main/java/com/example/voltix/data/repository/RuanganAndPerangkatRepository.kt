package com.example.voltix.data.repository

import androidx.lifecycle.LiveData
import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.dao.RuanganDAO
import com.example.voltix.data.dao.RuanganPerangkatCrossRefDAO
import com.example.voltix.data.entity.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuanganAndPerangkatRepository @Inject constructor(
    private val perangkatDao: PerangkatDAO,
    private val ruanganDao: RuanganDAO,
    private val crossRefDao: RuanganPerangkatCrossRefDAO
) {

    // === Perangkat ===

    suspend fun insertPerangkat(perangkat: PerangkatEntity): Long {
        return perangkatDao.insertPerangkatAndGetId(perangkat)
    }

    suspend fun insertAndGetId(perangkat: PerangkatEntity): Long {
        return perangkatDao.insertPerangkat(perangkat)
    }

    suspend fun updatePerangkat(perangkat: PerangkatEntity) {
        perangkatDao.updatePerangkat(perangkat)
    }

    suspend fun deletePerangkat(perangkat: PerangkatEntity) {
        perangkatDao.deletePerangkat(perangkat)
    }


    suspend fun getPerangkatById(id: Int): PerangkatEntity? {
        return perangkatDao.getPerangkatById(id)
    }

    fun getAllPerangkat(): LiveData<List<PerangkatEntity>> {
        return perangkatDao.getAllPerangkatLive()
    }

    suspend fun getAllPerangkatList(): List<PerangkatEntity> {
        return perangkatDao.getAllPerangkat()
    }

    // === Ruangan ===

    suspend fun insertRuangan(ruangan: RuanganEntity): Long {
        return ruanganDao.insertRuangan(ruangan)
    }

    fun getAllRuangan(): LiveData<List<RuanganEntity>> {
        return ruanganDao.getAllRuangan()
    }

    // === Relasi Ruangan & Perangkat ===

    suspend fun insertCrossRef(crossRef: RuanganPerangkatCrossRef) {
        crossRefDao.insertRuanganPerangkatCrossRef(crossRef)
    }

    suspend fun deleteCrossRef(crossRef: RuanganPerangkatCrossRef) {
        crossRefDao.deleteRuanganPerangkatCrossRef(crossRef)
    }

    suspend fun getCrossRefByPerangkatId(perangkatId: Int): RuanganPerangkatCrossRef {
        return crossRefDao.getCrossRefByPerangkatId(perangkatId)
    }

    suspend fun getPerangkatByRuanganId(ruanganId: Int): List<PerangkatEntity> {
        val crossRefs = crossRefDao.getPerangkatByRuanganId(ruanganId)
        return crossRefs.mapNotNull { perangkatDao.getPerangkatById(it.perangkatId) }
    }

    suspend fun getPerangkatWithWaktuByRuanganId(ruanganId: Int): List<PerangkatWithWaktu> {
        return crossRefDao.getPerangkatWithWaktuByRuanganId(ruanganId)
    }

    fun getRuanganWithPerangkat(ruanganId: Int): LiveData<List<RuanganWithPerangkat>> {
        return ruanganDao.getRuanganWithPerangkat(ruanganId)
    }

    suspend fun getCrossRef(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef? {
        return crossRefDao.getCrossRefByRuanganAndPerangkatId(ruanganId, perangkatId)
    }

    suspend fun updateCrossRef(crossRef: RuanganPerangkatCrossRef) {
        crossRefDao.updateRuanganPerangkatCrossRef(crossRef)
    }
}
