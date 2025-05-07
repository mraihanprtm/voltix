package com.example.voltix.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.dao.RuanganDAO
import com.example.voltix.data.dao.RuanganPerangkatCrossRefDAO
import com.example.voltix.data.dao.SimulationDAO
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.RuanganWithPerangkat
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.data.entity.SimulationWithDevices
import com.example.voltix.data.entity.PerangkatWithWaktu
import java.time.LocalDateTime
import javax.inject.Inject

class SimulationRepository @Inject constructor(
    private val perangkatDAO: PerangkatDAO,
    private val ruanganDAO: RuanganDAO,
    private val ruanganPerangkatCrossRefDAO: RuanganPerangkatCrossRefDAO,
    private val simulationDAO: SimulationDAO
) {
    // Perangkat operations
    suspend fun insertPerangkat(perangkat: PerangkatEntity): Long {
        return perangkatDAO.insertPerangkat(perangkat)
    }

    suspend fun createSimulation(name: String, ruanganId: Int? = null): Int {
        val simulation = SimulationEntity(
            name = name,
            ruanganId = ruanganId,
            createdAt = LocalDateTime.now()
        )
        return simulationDAO.insertSimulation(simulation).toInt()
    }

    fun getAllPerangkatLive(): LiveData<List<PerangkatEntity>> {
        return perangkatDAO.getAllPerangkatLive()
    }

    suspend fun getAllPerangkat(): List<PerangkatEntity> {
        return perangkatDAO.getAllPerangkat()
    }

    suspend fun updatePerangkat(perangkat: PerangkatEntity) {
        perangkatDAO.updatePerangkat(perangkat)
    }

    suspend fun deletePerangkat(perangkat: PerangkatEntity) {
        perangkatDAO.deletePerangkat(perangkat)
    }

    // Ruangan operations
    suspend fun insertRuangan(ruangan: RuanganEntity): Long {
        return ruanganDAO.insertRuangan(ruangan)
    }

    fun getAllRuanganLive(): LiveData<List<RuanganEntity>> {
        return ruanganDAO.getAllRuangan()
    }

    suspend fun getAllRuangan(): List<RuanganEntity> {
        return ruanganDAO.getAllRuanganList()
    }

    suspend fun updateRuangan(ruangan: RuanganEntity) {
        ruanganDAO.updateRuangan(ruangan)
    }

    suspend fun deleteRuangan(ruangan: RuanganEntity) {
        ruanganDAO.deleteRuangan(ruangan)
    }

    // Ruangan-Perangkat operations
    suspend fun insertRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef) {
        ruanganPerangkatCrossRefDAO.insertRuanganPerangkatCrossRef(crossRef)
    }

    suspend fun updateRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef) {
        ruanganPerangkatCrossRefDAO.updateRuanganPerangkatCrossRef(crossRef)
    }

    suspend fun deleteRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef) {
        ruanganPerangkatCrossRefDAO.deleteRuanganPerangkatCrossRef(crossRef)
    }

    suspend fun getCrossRef(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef? {
        return ruanganPerangkatCrossRefDAO.getCrossRef(ruanganId, perangkatId)
    }

    suspend fun getPerangkatWithWaktuByRuanganId(ruanganId: Int): List<PerangkatWithWaktu> {
        val devices = ruanganPerangkatCrossRefDAO.getPerangkatWithWaktuByRuanganId(ruanganId)
        Log.d("SimulationRepository", "getPerangkatWithWaktuByRuanganId($ruanganId) returned ${devices.size} devices: $devices")
        return devices
    }

    suspend fun insertSimulationDevice(simulationId: Int, device: SimulationDeviceEntity) {
        simulationDAO.insertSimulationDevice(device)
    }

    suspend fun updateSimulationDevice(device: SimulationDeviceEntity) {
        simulationDAO.updateSimulationDevice(device)
    }

    suspend fun deleteSimulationDevice(simulationId: Int, deviceId: Int) {
        simulationDAO.deleteSimulationDevice(simulationId, deviceId)
    }

    suspend fun getAllSimulationsWithDevices(): List<SimulationWithDevices> {
        return simulationDAO.getAllSimulationsWithDevices()
    }

    suspend fun getSimulationWithDevices(simulationId: Int): List<SimulationWithDevices> {
        return simulationDAO.getSimulationWithDevices(simulationId)
    }

    suspend fun getAllSimulations(): List<SimulationEntity> {
        return simulationDAO.getAllSimulations()
    }
}