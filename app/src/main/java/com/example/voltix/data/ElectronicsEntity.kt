package com.example.voltix.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "electronics_table")
data class ElectronicsEntity(
    @PrimaryKey(autoGenerate = true) val electronicId: Int = 0,
    val name: String,
    val voltage: Int,
    val category: String,
    val timeStart: Long,
    val timeStop: Long,
    val quantity: Int
)
