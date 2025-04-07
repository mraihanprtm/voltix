package com.example.voltix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SimulasiPerangkat")
data class SimulasiPerangkatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val daya: Int,
    val kategori: String,
    val waktuNyala: String,
    val waktuMati: String
)
