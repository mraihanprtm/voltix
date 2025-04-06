package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.voltix.data.entity.SimulasiPerangkatCrossRef
import com.example.voltix.data.entity.SimulasiTagihanEntity
import com.example.voltix.data.relations.SimulasiWithPerangkat

@Dao
interface SimulasiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulasi(simulasi: SimulasiTagihanEntity): Long

    @Transaction
    @Query("SELECT * FROM simulasi_tagihan WHERE simulasiId = :simulasiId")
    suspend fun getSimulasiWithPerangkat(simulasiId: Int): SimulasiWithPerangkat

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulasiPerangkatCrossRef(ref: SimulasiPerangkatCrossRef)
}
