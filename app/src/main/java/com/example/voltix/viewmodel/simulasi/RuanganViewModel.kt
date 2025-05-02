package com.example.voltix.viewmodel.simulasi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.repository.RuanganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RuanganViewModel @Inject constructor(
    private val ruanganRepository: RuanganRepository
) : ViewModel() {
    val allRuangan: LiveData<List<RuanganEntity>> = ruanganRepository.allRuangan
    private val _namaRuangan = MutableStateFlow<String?>(null)
    val namaRuangan: StateFlow<String?> = _namaRuangan.asStateFlow()

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
}
