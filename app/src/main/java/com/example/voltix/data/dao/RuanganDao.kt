package com.example.voltix.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganWithPerangkat // Pastikan relasi ini sudah benar
import kotlinx.coroutines.flow.Flow

@Dao
interface RuanganDAO {
    @Insert
    suspend fun insertRuangan(ruangan: RuanganEntity): Long

    @Delete
    suspend fun deleteRuangan(ruangan: RuanganEntity)

    @Update
    suspend fun updateRuangan(ruangan: RuanganEntity)

    @Query("SELECT * FROM ruangan WHERE userFirebaseUid = :uid ORDER BY namaRuangan ASC")
    fun getAllRuanganByFirebaseUid(uid: String): Flow<List<RuanganEntity>>

    // Jika Anda butuh versi suspend function (ambil sekali)
    @Query("SELECT * FROM ruangan WHERE userFirebaseUid = :uid ORDER BY namaRuangan ASC")
    suspend fun getAllRuanganByFirebaseUidSuspend(uid: String): List<RuanganEntity>

    @Query("SELECT * FROM ruangan WHERE id = :id")
    suspend fun getRuanganById(id: Int): RuanganEntity? // Untuk detail atau edit

    @Query("SELECT namaRuangan FROM ruangan WHERE id = :ruanganId")
    fun getNamaRuangan(ruanganId: Int): Flow<String?>

    // Metode ini mengambil semua ruangan, mungkin tidak lagi dibutuhkan untuk dashboard
    // atau perlu dipertimbangkan penggunaannya.
    @Query("SELECT * FROM ruangan")
    fun getAllRuangan(): LiveData<List<RuanganEntity>>

    // Metode ini juga mengambil semua ruangan.
    @Query("SELECT * FROM ruangan")
    suspend fun getAllRuanganList(): List<RuanganEntity>

    @Transaction
    @Query("SELECT * FROM ruangan WHERE id = :ruanganId")
    fun getRuanganWithPerangkat(ruanganId: Int): Flow<RuanganWithPerangkat>

    // ---- TAMBAHKAN METODE BARU INI ----
    /**
     * Mengambil semua ruangan beserta perangkatnya untuk pengguna tertentu.
     * @param userFirebaseUid Firebase UID dari pengguna.
     * @return Flow yang berisi list RuanganWithPerangkat.
     */
    @Transaction
    @Query("SELECT * FROM ruangan WHERE userFirebaseUid = :userFirebaseUid ORDER BY namaRuangan ASC") // Tambahkan ORDER BY jika perlu
    fun getAllRuanganWithPerangkatForUser(userFirebaseUid: String): Flow<List<RuanganWithPerangkat>>
    // ---------------------------------
}