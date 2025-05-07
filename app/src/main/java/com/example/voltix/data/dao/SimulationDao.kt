package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.data.entity.SimulationWithDevices

@Dao
interface SimulationDAO {
    @Insert
    suspend fun insertSimulation(simulation: SimulationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulationDevice(device: SimulationDeviceEntity)

    @Update
    suspend fun updateSimulationDevice(device: SimulationDeviceEntity)

    @Query("DELETE FROM simulation_device WHERE simulationId = :simulationId AND deviceId = :deviceId")
    suspend fun deleteSimulationDevice(simulationId: Int, deviceId: Int)

    @Query("SELECT * FROM simulation")
    suspend fun getAllSimulations(): List<SimulationEntity>

    @Transaction
    @Query("SELECT * FROM simulation WHERE id = :simulationId")
    suspend fun getSimulationWithDevices(simulationId: Int): List<SimulationWithDevices>

    @Transaction
    @Query("SELECT * FROM simulation")
    suspend fun getAllSimulationsWithDevices(): List<SimulationWithDevices>
}