package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = GolonganListrikEntity::class,
            parentColumns = ["idGolonganListrik"],
            childColumns = ["jenisListrik"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val name: String,
    val email: String,
    val jenisListrik: Int,
    val isPrabayar: Boolean,
)