package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.voltix.data.SimulasiPerangkatEntity

@Dao
interface SimulasiPerangkatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(simulasi: SimulasiPerangkatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<SimulasiPerangkatEntity>)

    @Query("SELECT * FROM SimulasiPerangkat")
    fun getAll(): LiveData<List<SimulasiPerangkatEntity>>

    @Delete
    suspend fun delete(simulasi: SimulasiPerangkatEntity)

    @Update
    suspend fun update(simulasi: SimulasiPerangkatEntity)

    @Query("DELETE FROM SimulasiPerangkat")
    suspend fun clear()
}

