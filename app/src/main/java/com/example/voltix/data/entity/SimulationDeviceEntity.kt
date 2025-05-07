package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "simulation_device",
    foreignKeys = [
        ForeignKey(
            entity = SimulationEntity::class,
            parentColumns = ["id"],
            childColumns = ["simulationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SimulationDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val deviceId: Int = 0,
    val simulationId: Int,
    val nama: String,
    val daya: Int,
    val jumlah: Int,
    val jenis: jenis,
    val waktuNyala: LocalTime,
    val waktuMati: LocalTime
)
