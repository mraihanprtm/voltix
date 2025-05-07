package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.voltix.data.entity.PerangkatWithWaktu
import com.example.voltix.data.entity.RuanganPerangkatCrossRef

@Dao
interface RuanganPerangkatCrossRefDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef)

    @Update
    suspend fun updateRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef)

    @Delete
    suspend fun deleteRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef)

    @Query("""
        UPDATE ruangan_perangkat_cross_ref 
        SET waktuNyala = :waktuNyala, waktuMati = :waktuMati 
        WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId
    """)
    suspend fun updateWaktuByRuanganAndPerangkat(
        ruanganId: Int,
        perangkatId: Int,
        waktuNyala: Int,
        waktuMati: Int
    )

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE ruanganId = :ruanganId")
    suspend fun getPerangkatByRuanganId(ruanganId: Int): List<RuanganPerangkatCrossRef>

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE perangkatId = :perangkatId")
    suspend fun getRuanganByPerangkatId(perangkatId: Int): List<RuanganPerangkatCrossRef>

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE perangkatId = :perangkatId")
    suspend fun getCrossRefByPerangkatId(perangkatId: Int): RuanganPerangkatCrossRef

    @Query("""
        SELECT ruanganId, perangkatId, waktuNyala, waktuMati
        FROM ruangan_perangkat_cross_ref
        WHERE ruanganId = :ruanganId
    """)
    fun getPerangkatForRuangan(ruanganId: Int): LiveData<List<RuanganPerangkatCrossRef>>

    @Query("""
        SELECT p.id AS perangkatId, p.nama, p.daya, p.jumlah, p.jenis, c.waktuNyala, c.waktuMati
        FROM perangkat p
        INNER JOIN ruangan_perangkat_cross_ref c ON p.id = c.perangkatId
        WHERE c.ruanganId = :ruanganId
    """)
    suspend fun getPerangkatWithWaktuByRuanganId(ruanganId: Int): List<PerangkatWithWaktu>

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId LIMIT 1")
    suspend fun getCrossRefByRuanganAndPerangkatId(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef?

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId")
    suspend fun getCrossRef(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef?
}
