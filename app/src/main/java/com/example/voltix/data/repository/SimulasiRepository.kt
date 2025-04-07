package com.example.voltix.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.voltix.data.AppDatabase
import com.example.voltix.data.SimulasiPerangkatEntity
import com.example.voltix.data.dao.SimulasiPerangkatDao

class SimulasiRepository(private val dao: SimulasiPerangkatDao) {

    val semuaSimulasi: LiveData<List<SimulasiPerangkatEntity>> = dao.getAll()

    suspend fun tambah(simulasi: SimulasiPerangkatEntity) = dao.insert(simulasi)
    suspend fun tambahSemua(list: List<SimulasiPerangkatEntity>) = dao.insertAll(list)
    suspend fun hapus(simulasi: SimulasiPerangkatEntity) = dao.delete(simulasi)
    suspend fun update(simulasi: SimulasiPerangkatEntity) = dao.update(simulasi)
    suspend fun clear() = dao.clear()

    companion object {
        @Volatile
        private var INSTANCE: SimulasiRepository? = null

        fun getInstance(context: Context): SimulasiRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context)
                val instance = SimulasiRepository(database.simulasiDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
