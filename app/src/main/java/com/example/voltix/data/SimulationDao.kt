package com.example.voltix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SimulationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulationData: SimulationEntity)

    @Update
    suspend fun updateSimulation(simulationData: SimulationEntity)

    @Delete
    suspend fun deleteSimulation(simulationData: SimulationEntity)

    @Query("SELECT * FROM simulation_table WHERE name = :name")
    suspend fun getSimulationbyName(name: String): List<SimulationEntity>?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElectronicDevice(electronicDevice: ElectronicsEntity)

    @Query("SELECT * FROM electronics_table WHERE name = :name")
    suspend fun getElectronicbyName(name: String): ElectronicsEntity

    @Update
    suspend fun updateElectronicDevice(electronicDevice: ElectronicsEntity)

    @Delete
    suspend fun deleteElectronicDevice(electronicDevice: ElectronicsEntity)

    @Query("SELECT * FROM electronics_table")
    fun getAllElectronicDevices(): List<ElectronicsEntity>


    @Insert
    suspend fun insertElectronicSimulation(electronicsSimulation: ElectronicsSimulation)

    @Update
    suspend fun updateElectronicSimulation(electronicsSimulation: ElectronicsSimulation)

    @Delete
    suspend fun deleteElectronicsSimulation(electronicsSimulation: ElectronicsSimulation)

    @Transaction
    @Query("SELECT * FROM simulation_table WHERE simulationId = :simulationId")
    suspend fun getSimulationwithElectronics(simulationId: Int): SimulationwithElectronics
}