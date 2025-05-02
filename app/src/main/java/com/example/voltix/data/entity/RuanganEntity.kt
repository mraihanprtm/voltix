package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ruangan")
data class RuanganEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaRuangan: String
)
