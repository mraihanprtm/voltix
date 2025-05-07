package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganWithPerangkat
import kotlinx.coroutines.flow.Flow

@Dao
interface RuanganDAO {
    @Insert
    suspend fun insertRuangan(ruangan: RuanganEntity): Long

    @Delete
    suspend fun deleteRuangan(ruangan: RuanganEntity)

    @Update
    suspend fun updateRuangan(ruangan: RuanganEntity)

    @Query("SELECT namaRuangan FROM ruangan WHERE id = :ruanganId")
    fun getNamaRuangan(ruanganId: Int): Flow<String?>

    @Query("SELECT * FROM ruangan")
    fun getAllRuangan(): LiveData<List<RuanganEntity>>

    @Query("SELECT * FROM ruangan")
    suspend fun getAllRuanganList(): List<RuanganEntity>

    @Transaction
    @Query("SELECT * FROM ruangan WHERE id = :ruanganId")
    fun getRuanganWithPerangkat(ruanganId: Int): LiveData<List<RuanganWithPerangkat>>
}