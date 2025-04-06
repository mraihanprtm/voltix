package com.example.voltix.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    primaryKeys = ["electronicId", "simulationId"],
    foreignKeys = [
        ForeignKey(
            entity = ElectronicsEntity::class,
            parentColumns = ["electronicId"],
            childColumns = ["electronicId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SimulationEntity::class,
            parentColumns = ["simulationId"],
            childColumns = ["simulationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("electronicId"), Index("simulationId")]
)
data class ElectronicsSimulation(
    val electronicId: Int,
    val simulationId: Int
)

data class ElectronicwithSimulations(
    @Embedded val electronic: ElectronicsEntity,

    @Relation(
        parentColumn = "electronicId",
        entityColumn = "simulationId",
        associateBy = Junction(ElectronicsSimulation::class)
    )
    val simulations: List<SimulationEntity>
)

data class SimulationwithElectronics(
    @Embedded val simulation: SimulationEntity,

    @Relation(
        parentColumn = "simulationId",
        entityColumn = "electronicId",
        associateBy = Junction(ElectronicsSimulation::class)
    )
    val electronics: List<ElectronicsEntity>
)