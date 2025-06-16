package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.RuanganWithPerangkat
import kotlinx.coroutines.flow.Flow

@Dao
interface PerangkatDAO {

    @Insert
    suspend fun insertPerangkat(perangkat: PerangkatEntity): Long

    @Insert
    suspend fun insertPerangkatAndGetId(perangkat: PerangkatEntity): Long

    @Update
    suspend fun updatePerangkat(perangkat: PerangkatEntity)

    @Delete
    suspend fun deletePerangkat(perangkat: PerangkatEntity)

    @Query("SELECT * FROM perangkat WHERE id = :id")
    suspend fun getPerangkatById(id: Int): PerangkatEntity?

    @Query("SELECT * FROM perangkat WHERE isDeleted = 0")
    suspend fun getAllPerangkat(): List<PerangkatEntity>

    @Query("SELECT * FROM perangkat WHERE isDeleted = 0")
    fun getAllPerangkatLive(): LiveData<List<PerangkatEntity>>

    @Insert
    suspend fun insertLampu(lampu: LampuEntity)

    @Update
    suspend fun updateLampu(lampu: LampuEntity)

    @Query("DELETE FROM lampu WHERE perangkatId = :perangkatId")
    suspend fun deleteLampuByPerangkatId(perangkatId: Int)

    @Query("SELECT * FROM lampu WHERE perangkatId = :perangkatId")
    suspend fun getLampuByPerangkatId(perangkatId: Int): LampuEntity?

    @Transaction
    @Query("SELECT * FROM ruangan WHERE isDeleted = 0 ORDER BY namaRuangan ASC")
    fun getAllRuanganWithPerangkat(): Flow<List<RuanganWithPerangkat>>

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId")
    suspend fun getCrossRef(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef?

    @Insert
    suspend fun insertAllPerangkat(perangkat: List<PerangkatEntity>)

    @Insert
    suspend fun insertAllLampu(lampu: List<LampuEntity>)

    @Query("SELECT * FROM perangkat WHERE lastModified > :timestamp AND isDeleted = 0")
    suspend fun getChangedSince(timestamp: Long): List<PerangkatEntity>

    @Query("SELECT * FROM perangkat WHERE isDeleted = 1")
    suspend fun getDeleted(): List<PerangkatEntity>

    @Query("SELECT * FROM lampu WHERE lastModified > :timestamp AND isDeleted = 0")
    suspend fun getLampuChangedSince(timestamp: Long): List<LampuEntity>

    @Query("SELECT * FROM lampu WHERE isDeleted = 1")
    suspend fun getLampuDeleted(): List<LampuEntity>
}