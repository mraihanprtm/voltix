package com.example.voltix.ui.simulasi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganWithPerangkat
import com.example.voltix.data.repository.RuanganAndPerangkatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimulasiViewModel @Inject constructor(
    private val repository: RuanganAndPerangkatRepository // Inject repository
) : ViewModel() {

    // Mode Bebas: Menampilkan semua perangkat
    val perangkatBebas: LiveData<List<PerangkatEntity>> = repository.getAllPerangkat()

    // Mode Berdasarkan Ruangan: Menampilkan perangkat berdasarkan ruangan yang dipilih
    private val _selectedRuanganId = MutableLiveData<Int?>()
    val perangkatByRuangan: LiveData<List<RuanganWithPerangkat>> = _selectedRuanganId.switchMap { id ->
        if (id == null) {
            MutableLiveData(emptyList()) // Jika tidak ada ruangan yang dipilih, tampilkan kosong
        } else {
            repository.getRuanganWithPerangkat(id) // Ambil perangkat berdasarkan ruangan
        }
    }

    // Daftar Ruangan untuk dipilih (misalnya untuk simulasi berdasarkan ruangan)
    val allRuangan: LiveData<List<RuanganEntity>> = repository.getAllRuangan()

    // Fungsi untuk memilih ruangan dan memicu pembaruan perangkat berdasarkan ruangan
    fun selectRuangan(id: Int) {
        _selectedRuanganId.value = id
    }
}
