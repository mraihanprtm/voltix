package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.voltix.data.entity.PerangkatEntity

@Dao
interface PerangkatDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerangkat(perangkat: PerangkatEntity): Long  // Ubah return type jadi Long untuk mendapatkan ID

    @Query("SELECT * FROM perangkat")
    fun getAllPerangkatLive(): LiveData<List<PerangkatEntity>>

    @Query("SELECT * FROM perangkat WHERE id = :id")
    suspend fun getPerangkatById(id: Int): PerangkatEntity?

    @Query("SELECT * FROM perangkat")
    suspend fun getAllPerangkat(): List<PerangkatEntity>

    @Delete
    suspend fun deletePerangkat(perangkat: PerangkatEntity)

    // Di PerangkatDAO
    @Update
    suspend fun updatePerangkat(perangkat: PerangkatEntity)

    @Insert
    suspend fun insertPerangkatAndGetId(perangkat: PerangkatEntity): Long
}