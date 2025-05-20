package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "BiayaPemakaian",
    primaryKeys = ["idGolonganListrik", "minKWH"],
    foreignKeys = [
        ForeignKey(
            entity = GolonganListrikEntity::class,
            parentColumns = ["idGolonganListrik"],
            childColumns = ["idGolonganListrik"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BiayaPemakaianEntity(
    val idGolonganListrik: Int,
    val minKWH: Int,
    val biayaReguler: Int,
    val biayaPrabayar: Int
)