package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class JenisRuangan {
    KamarTidur,
    RuangTamu,
    Dapur,
    KamarMandi,
    Lainnya
}

@Entity(tableName = "ruangan")
data class RuanganEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaRuangan: String,
    val panjangRuangan: Float, // dalam meter
    val lebarRuangan: Float,   // dalam meter
    val jenisRuangan: JenisRuangan
)