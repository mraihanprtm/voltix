package com.example.voltix.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import java.time.LocalTime

data class RuanganWithPerangkat(
    @Embedded val ruangan: RuanganEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RuanganPerangkatCrossRef::class,
            parentColumn = "ruanganId",
            entityColumn = "perangkatId"
        )
    )
    val perangkatList: List<PerangkatEntity>
)
data class PerangkatWithWaktu(
    val perangkatId: Int,
    val nama: String,
    val daya: Int,
    val waktuNyala: LocalTime,
    val waktuMati: LocalTime
)

