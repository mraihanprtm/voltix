package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "simulation")
data class SimulationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ruanganId: Int? = null, // Nullable for free simulations
    val createdAt: LocalDateTime = LocalDateTime.now()
)