package com.example.voltix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulation_table")
data class SimulationEntity(
    @PrimaryKey (autoGenerate = true) val simulationId: Int = 0,
    val name: String,
    val powerTotal: Float,
    val estimatedDailyBill: Float,
)
