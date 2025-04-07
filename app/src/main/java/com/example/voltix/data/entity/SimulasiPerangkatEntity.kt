package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "SimulasiPerangkat")
data class SimulasiPerangkatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val perangkatId: Int = 0,
    val nama: String,
    val daya: Int,
    val kategori: KategoriPerangkat,
    val waktuNyala: LocalTime,
    val waktuMati: LocalTime,
    val durasi: Float
)
