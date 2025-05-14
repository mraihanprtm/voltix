package com.example.voltix.viewmodel.simulasi

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganWithPerangkat
import com.example.voltix.data.repository.RuanganAndPerangkatRepository
import com.example.voltix.data.repository.RuanganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RuanganViewModel @Inject constructor(
    private val ruanganRepository: RuanganRepository,
    private val rpRepository: RuanganAndPerangkatRepository
) : ViewModel() {
    // Expose list of rooms as LiveData
    val allRuangan: LiveData<List<RuanganEntity>> = ruanganRepository.allRuangan

    // Backing state for detail
    private val _ruanganDetail = MutableStateFlow<RuanganWithPerangkat?>(null)
    val ruanganDetail: StateFlow<RuanganWithPerangkat?> = _ruanganDetail.asStateFlow()

    // Original namaRuangan state (if still needed)
    private val _namaRuangan = MutableStateFlow<String?>(null)
    val namaRuangan: StateFlow<String?> = _namaRuangan.asStateFlow()

    // di RuanganViewModel
    private val _lampuWithPerangkat = MutableStateFlow<List<LampWithPerangkat>>(emptyList())
    val lampuWithPerangkat: StateFlow<List<LampWithPerangkat>> = _lampuWithPerangkat

    fun loadLampuFor(ruanganId: Int) {
        viewModelScope.launch {
            // ambil PerangkatWithWaktu (ada perangkatId)
            val pwList = rpRepository.getPerangkatWithWaktuByRuanganId(ruanganId)
            val combined = pwList.mapNotNull { pw ->
                // ambil LampuEntity
                val lampu = rpRepository.getLampuByPerangkatId(pw.perangkatId)
                // ambil PerangkatEntity
                val perangkat = rpRepository.getPerangkatById(pw.perangkatId)
                if (lampu != null && perangkat != null) {
                    LampWithPerangkat(lampu, perangkat)
                } else null
            }
            _lampuWithPerangkat.value = combined
        }
    }

    fun insertRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            ruanganRepository.insertRuangan(ruangan)
        }
    }

    fun deleteRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            ruanganRepository.deleteRuangan(ruangan)
        }
    }

    fun updateRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            ruanganRepository.updateRuangan(ruangan)
        }
    }

    fun loadNamaRuangan(ruanganId: Int) {
        viewModelScope.launch {
            ruanganRepository.getNamaRuangan(ruanganId).collect { nama ->
                _namaRuangan.value = nama
            }
        }
    }

    // Load single room with perangkat
    fun loadDetail(ruanganId: Int) {
        viewModelScope.launch {
            ruanganRepository.getRuanganWithPerangkat(ruanganId)
                .collect { detail ->
                    _ruanganDetail.value = detail
                }
        }
    }
}
