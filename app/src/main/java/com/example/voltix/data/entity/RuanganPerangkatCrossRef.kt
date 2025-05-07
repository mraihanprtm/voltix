package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalTime

@Entity(
    tableName = "ruangan_perangkat_cross_ref",
    primaryKeys = ["ruanganId", "perangkatId"],
    foreignKeys = [
        ForeignKey(
            entity = RuanganEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruanganId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PerangkatEntity::class,
            parentColumns = ["id"],
            childColumns = ["perangkatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ruanganId"),
        Index("perangkatId")
    ]
)
data class RuanganPerangkatCrossRef(
    val ruanganId: Int,
    val perangkatId: Int,
    val waktuNyala: LocalTime,
    val waktuMati: LocalTime,
)