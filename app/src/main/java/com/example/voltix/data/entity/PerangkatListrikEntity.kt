package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perangkat_listrik")
data class PerangkatListrikEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val daya: Int,
    val kategori: String,
    val waktuNyala: String,
    val waktuMati: String
)
