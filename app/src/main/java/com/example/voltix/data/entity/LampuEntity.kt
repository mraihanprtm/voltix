package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.UUID

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
        Index("perangkatId"),
        Index(value = ["uuid"], unique = true)
    ]
)
data class LampuEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("perangkat_id")
    val perangkatId: Int, // Kolom untuk relasi dengan PerangkatEntity
    val jenis: jenisLampu,
    val lumen: Int,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val uuid: String = UUID.randomUUID().toString(),
)

