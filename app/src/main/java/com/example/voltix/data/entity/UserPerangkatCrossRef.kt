package com.example.voltix.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_perangkat_cross_ref",
    primaryKeys = ["userId", "perangkatId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PerangkatEntity::class,
            parentColumns = ["id"],
            childColumns = ["perangkatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("perangkatId")
    ]
)
data class UserPerangkatCrossRef(
    val userId: Int,
    val perangkatId: Int
)