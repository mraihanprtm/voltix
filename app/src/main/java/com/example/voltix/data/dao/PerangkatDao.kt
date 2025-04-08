package com.example.voltix.data.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.voltix.data.entity.PerangkatListrikEntity

@Dao
interface PerangkatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerangkat(perangkat: PerangkatListrikEntity)

    @Update
    suspend fun updatePerangkat(perangkat: PerangkatListrikEntity)

    @Delete
    suspend fun deletePerangkat(perangkat: PerangkatListrikEntity)

    @Query("SELECT * FROM PerangkatListrikEntity")
    fun getAllPerangkat(): LiveData<List<PerangkatListrikEntity>>

    @Query("SELECT * FROM PerangkatListrikEntity WHERE perangkatId = :id")
    suspend fun getPerangkatById(id: Int): PerangkatListrikEntity?

}
