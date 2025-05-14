package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.voltix.data.entity.RekomendasiDetail
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity

@Dao
interface RekomendasiDao {
    @Insert
    suspend fun insert(rekom: RekomendasiPenghematanLampuEntity): Long

    @Transaction
    @Query("""
        SELECT * FROM rekomendasi_penghematan_lampu
        WHERE id = :id
    """)
    suspend fun getDetailById(id: Int): RekomendasiDetail

    @Transaction
    @Query("""
        SELECT * FROM rekomendasi_penghematan_lampu
        WHERE userId = :userId
    """)
    suspend fun getHistoryByUser(userId: Int): List<RekomendasiDetail>

    @Transaction
    @Query("""
        SELECT * FROM rekomendasi_penghematan_lampu
    """)
    suspend fun getPublicRecommendations(): List<RekomendasiDetail>
}