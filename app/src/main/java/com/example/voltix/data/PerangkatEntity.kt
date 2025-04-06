package com.example.voltix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Perangkat")
data class PerangkatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val daya: Int,
    val durasi: Float,
)
