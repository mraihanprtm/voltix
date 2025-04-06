package com.example.voltix.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.AppDatabase
import com.example.voltix.data.Perangkat
import com.example.voltix.data.PerangkatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerangkatViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).perangkatDao()
    val perangkatList: LiveData<List<PerangkatEntity>> = dao.getAllPerangkat()
    var perangkatDiedit by mutableStateOf<Perangkat?>(null)
    var showEditDialog by mutableStateOf(false)
    var showTambahDialog by mutableStateOf(false)

    fun insertPerangkat(nama: String, daya: Int, durasi: Float) {
        viewModelScope.launch {
            val perangkat = PerangkatEntity(
                nama = nama,
                daya = daya,
                durasi = durasi
            )
            dao.insertPerangkat(perangkat)
        }
    }

    fun updatePerangkat(perangkat: PerangkatEntity) {
        viewModelScope.launch {
            dao.updatePerangkat(perangkat)
        }
    }

    fun deletePerangkat(perangkat: PerangkatEntity) {
        viewModelScope.launch {
            dao.deletePerangkat(perangkat)
        }
    }

    suspend fun getPerangkatById(id: Int): PerangkatEntity? {
        return withContext(Dispatchers.IO) {
            dao.getPerangkatById(id)
        }
    }
}
