// RekomendasiDetail.kt
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
    val lampu: LampuEntity
)