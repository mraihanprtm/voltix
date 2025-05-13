package com.example.voltix.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RekomendasiDetail(
    @Embedded
    val rekomendasi: RekomendasiPenghematanLampuEntity,

    @Relation(
        parentColumn = "ruanganId",
        entityColumn = "id"
    )
    val ruangan: RuanganEntity,

    @Relation(
        parentColumn = "lampuId",
        entityColumn = "id"
    )
    val lampu: LampuEntity,

    // kalau butuh tahu nama & daya perangkat umum:
    @Relation(
        parentColumn = "perangkatId",
        entityColumn = "id",
        entity = PerangkatEntity::class,
        associateBy = androidx.room.Junction(
            value = LampuEntity::class,
            parentColumn = "id",
            entityColumn = "perangkatId"
        )
    )
    val perangkat: PerangkatEntity
)
