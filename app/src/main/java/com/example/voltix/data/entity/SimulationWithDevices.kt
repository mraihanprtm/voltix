package com.example.voltix.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SimulationWithDevices(
    @Embedded val simulation: SimulationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "simulationId"
    )
    val devices: List<SimulationDeviceEntity>
)