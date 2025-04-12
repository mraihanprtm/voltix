package com.example.voltix.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.KategoriPerangkat
import com.example.voltix.data.entity.PerangkatListrikEntity
//import com.example.voltix.data.Perangkat
import com.example.voltix.repository.PerangkatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class PerangkatViewModel @Inject constructor(
    private val repository: PerangkatRepository

) : ViewModel() {

    val perangkatList: LiveData<List<PerangkatListrikEntity>> = repository.allPerangkat
    var perangkatDiedit by mutableStateOf<PerangkatListrikEntity?>(null)
    var showEditDialog by mutableStateOf(false)
    var waktuNyalaBaru by mutableStateOf(LocalTime.of(6, 0))
    var waktuMatiBaru by mutableStateOf(LocalTime.of(18, 0))

    val durasiBaru: Float
        get() = hitungDurasi()

    fun hitungDurasi(): Float {
        val durasiMenit = java.time.Duration.between(waktuNyalaBaru, waktuMatiBaru).toMinutes()
        return if (durasiMenit > 0) durasiMenit / 60f else 0f
    }

    fun insertPerangkat(
        nama: String,
        daya: Int,
        kategori: KategoriPerangkat,
        waktuNyala: LocalTime,
        waktuMati: LocalTime,
        durasibaru: Float
    ) {
        viewModelScope.launch {
            val perangkat = PerangkatListrikEntity(
                nama = nama,
                daya = daya,
                kategori = kategori,
                waktuNyala = waktuNyala,
                waktuMati = waktuMati,
                durasi = durasibaru,
            )
            repository.insert(perangkat)
        }
    }

    fun updatePerangkat(perangkat: PerangkatListrikEntity) {
        viewModelScope.launch {
            repository.update(perangkat)
        }
    }

    fun deletePerangkat(perangkat: PerangkatListrikEntity) {
        viewModelScope.launch {
            repository.delete(perangkat)
        }
    }

    suspend fun getPerangkatById(id: Int): PerangkatListrikEntity? {
        return repository.getById(id)
    }

    fun editPerangkat(
        nama: String,
        daya: Int,
        kategori: KategoriPerangkat,
        waktuNyala: LocalTime,
        waktuMati: LocalTime,
        durasi: Float
    ) {
        perangkatDiedit?.let {
            // Pastikan nama, daya, kategori, waktuNyala, waktuMati, dan durasi menggunakan parameter yang benar
            val updated = it.copy(
                nama = nama,
                daya = daya,
                kategori = kategori,
                waktuNyala = waktuNyala,
                waktuMati = waktuMati,
                durasi = durasi // Gunakan durasi yang dihitung sebelumnya
            )
            viewModelScope.launch {
                // Pastikan repository melakukan pembaruan perangkat dengan benar
                repository.update(updated)
            }
            perangkatDiedit = null
        }
        showEditDialog = false
    }

}
