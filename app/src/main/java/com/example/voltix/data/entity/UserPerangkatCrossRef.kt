package com.example.voltix.data.entity

import androidx.room.Entity

@Entity(
    primaryKeys = ["userId", "perangkatId"],
    tableName = "user_perangkat_cross_ref"
)
data class UserPerangkatCrossRef(
    val userId: Int,
    val perangkatId: Int
)

