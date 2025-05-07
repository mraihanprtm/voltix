package com.example.voltix.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.voltix.data.entity.PerangkatEntity
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
    val perangkat: List<PerangkatEntity>
)