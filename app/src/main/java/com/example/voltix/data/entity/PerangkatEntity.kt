package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class jenis {
    Lainnya,
    Lampu
}

@Entity(tableName = "perangkat",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class PerangkatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val jumlah: Int,
    val daya: Int, // dalam watt
    val jenis: jenis,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val uuid: String = UUID.randomUUID().toString(),
)
