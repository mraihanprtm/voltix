package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class jenis {
    Lainnya,
    Lampu
}

@Entity(tableName = "perangkat")
data class PerangkatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val jumlah: Int,
    val daya: Int, // dalam watt
    val jenis: jenis
)
