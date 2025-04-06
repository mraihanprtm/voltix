package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "simulasi_tagihan",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SimulasiTagihanEntity(
    @PrimaryKey(autoGenerate = true) val simulasiId: Int = 0,
    val namaSet: String,
    val totalDaya: Int,
    val estimasiTagihan: Double,
    val userId: Int
)
