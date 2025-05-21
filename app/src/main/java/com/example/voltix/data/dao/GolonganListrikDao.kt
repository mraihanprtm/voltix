package com.example.voltix.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.voltix.data.entity.BiayaPemakaianEntity
import com.example.voltix.data.entity.GolonganListrikDenganBiaya
import com.example.voltix.data.entity.GolonganListrikEntity

@Dao
interface GolonganListrikDao {
    @Insert
    suspend fun insertGolonganListrik(golongan: GolonganListrikEntity): Long

    @Update
    suspend fun updateGolonganListrik(golongan: GolonganListrikEntity)

    @Delete
    suspend fun deleteGolonganListrik(golongan: GolonganListrikEntity)

    @Query("SELECT * FROM golonganListrik")
    suspend fun getAllGolonganListrik(): List<GolonganListrikEntity>

    @Insert
    suspend fun insertBiayaPemakaian(biayaPemakaian: BiayaPemakaianEntity)

    @Update
    suspend fun updateBiayaPemakaian(biayaPemakaian: BiayaPemakaianEntity)

    @Delete
    suspend fun deleteBiayaPemakaian(biayaPemakaian: BiayaPemakaianEntity)

    @Query("SELECT * FROM biayapemakaian")
    suspend fun getAllBiayaPemakaian(): List<BiayaPemakaianEntity>

    @Query("SELECT * FROM biayapemakaian WHERE idGolonganListrik = :idGolonganListrik")
    suspend fun getBiayaPemakaianbyGolonganID(idGolonganListrik: Int): List<BiayaPemakaianEntity>

    @Query("""
        SELECT g.idGolonganListrik,
        g.golonganTarif,
        g.batasDaya,
        g.isRTM,
        g.biayaBeban,
        b.minKWH,
        b.biayaReguler,
        b.biayaPrabayar
        FROM golonganListrik g
        INNER JOIN BiayaPemakaian b
        ON g.idGolonganListrik = b.idGolonganListrik
        WHERE g.idGolonganListrik = :userGolonganListrik
    """)
    suspend fun getBiayaTarifListrik(userGolonganListrik: Int): List<GolonganListrikDenganBiaya>


    @Insert
    suspend fun insertAllGolonganListrik(vararg golonganListrik: GolonganListrikEntity)

    @Insert
    suspend fun insertAllBiayaPemakaian(vararg biayaPemakaian: BiayaPemakaianEntity)

    @Query("""
        SELECT DISTINCT g.idGolonganListrik,
        g.golonganTarif,
        g.batasDaya,
        g.isRTM,
        g.biayaBeban,
        b.minKWH,
        b.biayaReguler,
        b.biayaPrabayar
        FROM golonganListrik g
        INNER JOIN BiayaPemakaian b
        ON g.idGolonganListrik = b.idGolonganListrik
    """)
    suspend fun getAllGolonganListrikwithBiaya(): List<GolonganListrikDenganBiaya>
}

