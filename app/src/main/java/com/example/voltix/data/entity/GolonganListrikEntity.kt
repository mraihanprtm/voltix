package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "golonganListrik",
)
data class GolonganListrikEntity(
    @PrimaryKey(autoGenerate = true)
    val idGolonganListrik: Int = 0,
    val golonganTarif: String,
    val batasDaya: Int,
    val biayaBeban: Int,
    val isRTM: Boolean
)

data class GolonganListrikDenganBiaya(
    val idGolonganListrik: Int,
    val golonganTarif: String,
    val batasDaya: Int,
    val isRTM: Boolean,
    val biayaBeban: Int,
    val minKWH: Int,
    val biayaReguler: Int,
    val biayaPrabayar: Int
    )