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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class DevicePowerUsage(
    val deviceName: String,
    val hourlyPower: List<Float>
)

data class DashboardData(
    val totalDevices: Int,
    val totalPower: Float, // in watts
    val totalCost: Float, // in IDR
    val hourlyPower: List<Float> // Total power for each hour (0-23)
)

class SimulationRepository @Inject constructor(
    private val perangkatDAO: PerangkatDAO,
    private val ruanganDAO: RuanganDAO,
    private val ruanganPerangkatCrossRefDAO: RuanganPerangkatCrossRefDAO,
    private val simulationDAO: SimulationDAO,
    private val userRepository: UserRepository
) {

    fun calculateTotalCost(totalPower: Float, tarif: Int): Float {
        return totalPower * tarif
    }

    suspend fun getDashboardData(userId: Int): DashboardData {
        val ruanganList = perangkatDAO.getAllRuanganWithPerangkat().first()
        var totalDevices = 0
        val hourlyPower = MutableList(24) { 0f }

        ruanganList.forEach { ruangan ->
            ruangan.perangkatList.forEach { perangkat ->
                totalDevices++
                val crossRef = perangkatDAO.getCrossRef(ruangan.ruangan.id, perangkat.id)
                    ?: RuanganPerangkatCrossRef(
                        ruanganId = ruangan.ruangan.id,
                        perangkatId = perangkat.id,
                        waktuNyala = LocalTime.of(6, 0),
                        waktuMati = LocalTime.of(22, 0)
                    )

                val powerPerUnit = perangkat.daya * perangkat.jumlah
                val startHour = crossRef.waktuNyala.hour
                val endHour = crossRef.waktuMati.hour

                if (startHour < endHour) {
                    for (hour in startHour until endHour) {
                        hourlyPower[hour] += powerPerUnit.toFloat()
                    }
                } else {
                    for (hour in startHour until 24) {
                        hourlyPower[hour] += powerPerUnit.toFloat()
                    }
                    for (hour in 0 until endHour) {
                        hourlyPower[hour] += powerPerUnit.toFloat()
                    }
                }
            }
        }

        val totalPower = hourlyPower.sum() / 1000f // kWh
        val tarif = userRepository.getUserTarif(userId, totalPower.toDouble())
        val totalCost = calculateTotalCost(totalPower, tarif)

        return DashboardData(
            totalDevices = totalDevices,
            totalPower = totalPower,
            totalCost = totalCost,
            hourlyPower = hourlyPower
        )
    }

    suspend fun getTotalPower(): Float {
        val ruanganList = perangkatDAO.getAllRuanganWithPerangkat().first()
        val hourlyPower = MutableList(24) { 0f }

        ruanganList.forEach { ruangan ->
            ruangan.perangkatList.forEach { perangkat ->
                val crossRef = perangkatDAO.getCrossRef(ruangan.ruangan.id, perangkat.id)
                    ?: RuanganPerangkatCrossRef(
                        ruanganId = ruangan.ruangan.id,
                        perangkatId = perangkat.id,
                        waktuNyala = LocalTime.of(6, 0),
                        waktuMati = LocalTime.of(22, 0)
                    )

                val powerPerUnit = perangkat.daya * perangkat.jumlah
                val startHour = crossRef.waktuNyala.hour
                val endHour = crossRef.waktuMati.hour

                val hours = if (startHour < endHour) {
                    startHour until endHour
                } else {
                    (startHour until 24) + (0 until endHour)
                }

                for (hour in hours) {
                    hourlyPower[hour] += powerPerUnit.toFloat()
                }
            }
        }

        return hourlyPower.sum() / 1000f // in kWh
    }

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

    suspend fun updateSimulationName(simulationId: Int, newName: String) {
        simulationDAO.updateSimulationName(simulationId, newName)
    }

    suspend fun deleteSimulation(simulationId: Int) {
        simulationDAO.deleteSimulation(simulationId)
        simulationDAO.deleteDevicesBySimulationId(simulationId)
    }
}