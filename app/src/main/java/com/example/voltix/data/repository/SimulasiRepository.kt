package com.example.voltix.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import com.example.voltix.data.dao.SimulasiPerangkatDao
import com.example.voltix.data.entity.SimulasiPerangkatEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Define the DataStore at file level
private val Context.simulationDataStore by preferencesDataStore(name = "simulation_stats")

data class Statistics(
    val totalDaya: Int,
    val totalKonsumsi: Double,
    val totalBiaya: Double
)

@Singleton
class SimulasiRepository @Inject constructor(
    private val dao: SimulasiPerangkatDao,
    @ApplicationContext private val context: Context // Add this parameter
) {
    // Reference to DataStore
    private val dataStore = context.simulationDataStore

    // Define preference keys
    private object PreferencesKeys {
        val TOTAL_DAYA = intPreferencesKey("total_daya")
        val TOTAL_KONSUMSI = doublePreferencesKey("total_konsumsi")
        val TOTAL_BIAYA = doublePreferencesKey("total_biaya")
        val SUDAH_DICLONE = booleanPreferencesKey("sudah_diclone")
    }

    val semuaSimulasi: LiveData<List<SimulasiPerangkatEntity>> = dao.getAll()

    suspend fun tambah(simulasi: SimulasiPerangkatEntity) = dao.insert(simulasi)
    suspend fun tambahSemua(list: List<SimulasiPerangkatEntity>) = dao.insertAll(list)
    suspend fun hapus(simulasi: SimulasiPerangkatEntity) = dao.delete(simulasi)
    suspend fun update(simulasi: SimulasiPerangkatEntity) = dao.update(simulasi)
    suspend fun clear() = dao.clear()

    // Methods for statistics using DataStore
    suspend fun saveStatistics(totalDaya: Int, totalKonsumsi: Double, totalBiaya: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_DAYA] = totalDaya
            preferences[PreferencesKeys.TOTAL_KONSUMSI] = totalKonsumsi
            preferences[PreferencesKeys.TOTAL_BIAYA] = totalBiaya
            preferences[PreferencesKeys.SUDAH_DICLONE] = true
        }
    }

    suspend fun getStatistics(): Statistics? {
        val preferences = dataStore.data.first()
        val isCloningDone = preferences[PreferencesKeys.SUDAH_DICLONE] ?: false

        return if (isCloningDone) {
            Statistics(
                totalDaya = preferences[PreferencesKeys.TOTAL_DAYA] ?: 0,
                totalKonsumsi = preferences[PreferencesKeys.TOTAL_KONSUMSI] ?: 0.0,
                totalBiaya = preferences[PreferencesKeys.TOTAL_BIAYA] ?: 0.0
            )
        } else null
    }

    suspend fun isCloningDone(): Boolean {
        return dataStore.data.first()[PreferencesKeys.SUDAH_DICLONE] ?: false
    }

    suspend fun resetCloneStatus() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUDAH_DICLONE] = false
        }
    }
}