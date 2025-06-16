package com.example.voltix.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

enum class JenisRuangan(val label: String) {
    KamarTidur("Kamar Tidur"),
    RuangTamu("Ruang Tamu"),
    Dapur("Dapur"),
    KamarMandi("Kamar Mandi"),
    Lainnya("Lainnya")
}

@Entity(tableName = "ruangan",
    indices = [Index(value = ["uuid"], unique = true)])
data class RuanganEntity(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ---- TAMBAHKAN FIELD INI ----
    @SerializedName("user_id")
    @ColumnInfo(index = true) // Menambahkan index pada kolom ini baik untuk performa query
    val userFirebaseUid: String, // Untuk menyimpan Firebase UID dari pemilik ruangan
    // --------------------------

    @SerializedName("nama_ruangan")
    val namaRuangan: String,

    @SerializedName("panjang_ruangan")
    val panjangRuangan: Float, // dalam meter

    @SerializedName("lebar_ruangan")
    val lebarRuangan: Float,   // dalam meter

    @SerializedName("jenis_ruangan")
    val jenisRuangan: JenisRuangan,

    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,

    val uuid: String = UUID.randomUUID().toString(),
)