package com.example.voltix.data


import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PerangkatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerangkat(perangkat: PerangkatEntity)

    @Update
    suspend fun updatePerangkat(perangkat: PerangkatEntity)

    @Delete
    suspend fun deletePerangkat(perangkat: PerangkatEntity)

    @Query("SELECT * FROM Perangkat")
    fun getAllPerangkat(): LiveData<List<PerangkatEntity>>

    @Query("SELECT * FROM Perangkat WHERE id = :id")
    suspend fun getPerangkatById(id: Int): PerangkatEntity?

}
