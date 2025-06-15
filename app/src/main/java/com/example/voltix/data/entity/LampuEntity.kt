package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class jenisLampu {
    Neon,
    LED,
}
// Entitas Lampu
@Entity(
    tableName = "lampu",
    foreignKeys = [
        ForeignKey(
            entity = PerangkatEntity::class,
            parentColumns = ["id"],
            childColumns = ["perangkatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("perangkatId")
    ]
)
data class LampuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val perangkatId: Int, // Kolom untuk relasi dengan PerangkatEntity
    val jenis: jenisLampu,
    val lumen: Int,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
)

