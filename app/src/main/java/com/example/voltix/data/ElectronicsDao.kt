package com.example.voltix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ElectronicsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(electronicDevice: ElectronicsEntity)

    @Query("SELECT * FROM electronics_table WHERE name = :name")
    suspend fun getElectronicbyName(name: String): ElectronicsEntity

    @Update
    suspend fun update(electronicDevice: ElectronicsEntity)

    @Delete
    suspend fun delete(electronicDevice: ElectronicsEntity)

    @Query("SELECT * FROM electronics_table")
    fun getAllElectronicDevices(): List<ElectronicsEntity>


}