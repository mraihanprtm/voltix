package com.example.voltix.data.entity

import androidx.room.Entity

@Entity(
    primaryKeys = ["simulasiId", "perangkatId"],
    tableName = "simulasi_perangkat_cross_ref"
)
data class SimulasiPerangkatCrossRef(
    val simulasiId: Int,
    val perangkatId: Int
)
