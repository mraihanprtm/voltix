package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["simulasiId", "perangkatId"],
    foreignKeys = [
        ForeignKey(
            entity = PerangkatListrikEntity::class,
            parentColumns = ["perangkatId"],
            childColumns = ["perangkatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SimulasiTagihanEntity::class,
            parentColumns = ["simulasiId"],
            childColumns = ["simulasiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("simulasiId"), Index("perangkatId")]
)
data class SimulasiPerangkatCrossRef(
    val simulasiId: Int,
    val perangkatId: Int
)
