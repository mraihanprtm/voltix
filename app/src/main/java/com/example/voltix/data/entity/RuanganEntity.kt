package com.example.voltix.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class JenisRuangan(val label: String) {
    KamarTidur("Kamar Tidur"),
    RuangTamu("Ruang Tamu"),
    Dapur("Dapur"),
    KamarMandi("Kamar Mandi"),
    Lainnya("Lainnya")
}

@Entity(tableName = "ruangan")
data class RuanganEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ---- TAMBAHKAN FIELD INI ----
    @ColumnInfo(index = true) // Menambahkan index pada kolom ini baik untuk performa query
    val userFirebaseUid: String, // Untuk menyimpan Firebase UID dari pemilik ruangan
    // --------------------------

    val namaRuangan: String,
    val panjangRuangan: Float, // dalam meter
    val lebarRuangan: Float,   // dalam meter
    val jenisRuangan: JenisRuangan,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
)