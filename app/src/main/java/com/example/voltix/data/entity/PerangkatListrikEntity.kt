package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalTime

// Definisi enum untuk kategori
enum class KategoriPerangkat {
    ELEKTRONIK,
    LAMPU,
    PENDINGIN,
    PEMANAS,
    DAPUR,
    LAINNYA
}

@Entity(tableName = "PerangkatListrikEntity")
data class PerangkatListrikEntity(
    @PrimaryKey(autoGenerate = true)
    val perangkatId: Int = 0,
    val nama: String,
    val daya: Int,
    val kategori: KategoriPerangkat,
    val waktuNyala: LocalTime,
    val waktuMati: LocalTime,
    val durasi: Float
)

