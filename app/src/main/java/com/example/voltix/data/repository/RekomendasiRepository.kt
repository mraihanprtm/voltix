package com.example.voltix.data.repository

import com.example.voltix.data.dao.RekomendasiDao
import com.example.voltix.data.entity.RekomendasiDetail
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RekomendasiRepository @Inject constructor(
    private val rekomDao: RekomendasiDao
) {

    suspend fun insertRekomendasi(rekom: RekomendasiPenghematanLampuEntity): Long {
        return rekomDao.insert(rekom)
    }

    suspend fun getRekomendasiDetail(id: Int): RekomendasiDetail {
        return rekomDao.getDetailById(id)
    }

    suspend fun getUserHistory(userId: Int): List<RekomendasiDetail> {
        return rekomDao.getHistoryByUser(userId)
    }

    suspend fun getPublicRekomendasi(): List<RekomendasiDetail> {
        return rekomDao.getPublicRecommendations()
    }
}
