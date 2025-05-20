package com.example.voltix.data.repository

import com.example.voltix.data.dao.PerangkatDAO
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.RuanganWithPerangkat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalTime
import java.time.temporal.ChronoUnit
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

class DashboardRepository @Inject constructor(
    private val perangkatDao: PerangkatDAO
) {
    fun getDashboardData(): Flow<DashboardData> = flow {
        val ruanganWithPerangkat = perangkatDao.getAllRuanganWithPerangkat()
        ruanganWithPerangkat.collect { ruanganList ->
            val costPerKWh = 1444.70f // IDR per kWh (adjust as needed)
            var totalDevices = 0
            var totalPower = 0f
            val hourlyPower = MutableList(24) { 0f }

            ruanganList.forEach { ruangan ->
                ruangan.perangkatList.forEach { perangkat ->
                    totalDevices++
                    val crossRef = perangkatDao.getCrossRef(ruangan.ruangan.id, perangkat.id)
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

            totalPower = (hourlyPower.sum()/1000f)
            val totalCost = totalPower * costPerKWh

            emit(
                DashboardData(
                    totalDevices = totalDevices,
                    totalPower = totalPower,
                    totalCost = totalCost,
                    hourlyPower = hourlyPower
                )
            )
        }
    }
}