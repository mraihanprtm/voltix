package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.voltix.data.entity.PerangkatWithWaktu
import com.example.voltix.data.entity.RuanganPerangkatCrossRef

@Dao
interface RuanganPerangkatCrossRefDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef)

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
        SELECT 
            p.id as perangkatId,
            p.nama as nama,
            p.daya as daya,
            rp.waktuNyala as waktuNyala,
            rp.waktuMati as waktuMati
        FROM perangkat p 
        INNER JOIN ruangan_perangkat_cross_ref rp 
        ON p.id = rp.perangkatId 
        WHERE rp.ruanganId = :ruanganId
    """)
    suspend fun getPerangkatWithWaktuByRuanganId(ruanganId: Int): List<PerangkatWithWaktu>

    @Query("SELECT * FROM ruangan_perangkat_cross_ref WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId LIMIT 1")
    suspend fun getCrossRefByRuanganAndPerangkatId(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef?

    @Update
    suspend fun updateRuanganPerangkatCrossRef(crossRef: RuanganPerangkatCrossRef)

    @Query("""
        SELECT * FROM ruangan_perangkat_cross_ref 
        WHERE ruanganId = :ruanganId AND perangkatId = :perangkatId 
        LIMIT 1
    """)
    suspend fun getCrossRef(ruanganId: Int, perangkatId: Int): RuanganPerangkatCrossRef?
}
