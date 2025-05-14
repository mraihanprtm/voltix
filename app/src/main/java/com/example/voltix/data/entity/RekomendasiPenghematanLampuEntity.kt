package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "rekomendasi_penghematan_lampu",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RuanganEntity::class,
            parentColumns = ["id"],
            childColumns = ["ruanganId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LampuEntity::class,
            parentColumns = ["id"],
            childColumns = ["lampuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("ruanganId"),
        Index("lampuId")
    ]
)
data class RekomendasiPenghematanLampuEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // jika kamu ingin simpan history per user
    val userId: Int? = null,

    // relasi ke ruangan
    val ruanganId: Int,

    // relasi ke lampu (yang sudah punya perangkatId di dalamnya)
    val lampuId: Int,

    // simpan tanggal rekomendasi
    @ColumnInfo(name = "tanggal") val tanggal: Date = Date()
)
