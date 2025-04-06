package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.voltix.data.entity.PerangkatListrikEntity

@Dao
interface PerangkatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerangkat(perangkat: PerangkatListrikEntity): Long

    @Query("SELECT * FROM perangkat_listrik")
    suspend fun getAllPerangkat(): List<PerangkatListrikEntity>
}
