package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.voltix.data.entity.UserPerangkatCrossRef

@Dao
interface UserPerangkatCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: UserPerangkatCrossRef)

    @Delete
    suspend fun delete(crossRef: UserPerangkatCrossRef)

    @Query("SELECT * FROM user_perangkat_cross_ref WHERE userId = :userId")
    suspend fun getCrossRefsByUserId(userId: Int): List<UserPerangkatCrossRef>
}