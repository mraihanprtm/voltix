package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.data.entity.PerangkatEntity

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

    @Query("SELECT * FROM perangkat")
    suspend fun getAllPerangkat(): List<PerangkatEntity>

    @Query("SELECT * FROM perangkat")
    fun getAllPerangkatLive(): LiveData<List<PerangkatEntity>>

    @Insert
    suspend fun insertLampu(lampu: LampuEntity)

    @Update
    suspend fun updateLampu(lampu: LampuEntity)

    @Query("DELETE FROM lampu WHERE perangkatId = :perangkatId")
    suspend fun deleteLampuByPerangkatId(perangkatId: Int)

    @Query("SELECT * FROM lampu WHERE perangkatId = :perangkatId")
    suspend fun getLampuByPerangkatId(perangkatId: Int): LampuEntity?
}