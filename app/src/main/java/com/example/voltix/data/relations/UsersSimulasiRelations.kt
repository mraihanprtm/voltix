package com.example.voltix.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.voltix.data.entity.PerangkatListrikEntity
import com.example.voltix.data.entity.SimulasiPerangkatCrossRef
import com.example.voltix.data.entity.SimulasiTagihanEntity
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.UserPerangkatCrossRef

data class UserWithPerangkat(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserPerangkatCrossRef::class,
            parentColumn = "userId",
            entityColumn = "perangkatId"
        )
    )
    val perangkatList: List<PerangkatListrikEntity>
)

data class SimulasiWithPerangkat(
    @Embedded val simulasi: SimulasiTagihanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SimulasiPerangkatCrossRef::class,
            parentColumn = "simulasiId",
            entityColumn = "perangkatId"
        )
    )
    val perangkatList: List<PerangkatListrikEntity>
)
