package com.example.voltix.viewmodel.simulasi // Pastikan package Anda benar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.RuanganWithPerangkat
import com.example.voltix.data.repository.RuanganAndPerangkatRepository
import com.example.voltix.data.repository.RuanganRepository // Pastikan ini adalah interface/class yang benar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RuanganViewModel @Inject constructor(
    private val ruanganRepository: RuanganRepository, // Ini harusnya interface RuanganRepository yang sudah didefinisikan
    private val rpRepository: RuanganAndPerangkatRepository
) : ViewModel() {

    // StateFlow untuk menyimpan firebaseUid pengguna saat ini
    private val _currentFirebaseUid = MutableStateFlow<String?>(null)

    // StateFlow untuk loading utama atau operasi umum
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mengubah allRuangan menjadi StateFlow yang bereaksi terhadap _currentFirebaseUid
    // Ini akan mengambil data dari ruanganRepository.getAllRuanganByFirebaseUid(uid)

    val allRuangan: StateFlow<List<RuanganEntity>> = _currentFirebaseUid.flatMapLatest { uid ->
        if (uid != null) {
            Log.d("RuanganViewModel", "UID active: $uid. Subscribing to getAllRuanganByFirebaseUid.")
            ruanganRepository.getAllRuanganByFirebaseUid(uid)
                .onStart { // Dipanggil setiap kali Flow ini mulai dikoleksi (misalnya saat kembali ke layar)
                    Log.d("RuanganViewModel", "Flow for UID $uid: .onStart -> _isLoading = true")
                    _isLoading.value = true
                }
                .map { data -> // Atau .onEach jika Anda tidak ingin mengubah data
                    Log.d("RuanganViewModel", "Flow for UID $uid: .map (data received, size: ${data.size}) -> _isLoading = false")
                    _isLoading.value = false // Data pertama diterima, loading selesai untuk emisi ini
                    data
                }
                .catch { e ->
                    Log.e("RuanganViewModel", "Flow for UID $uid: .catch (error: ${e.message}) -> _isLoading = false", e)
                    _isLoading.value = false
                    emit(emptyList())
                }
        } else {
            Log.d("RuanganViewModel", "UID is null. Setting _isLoading = false and emitting empty list.")
            flow { // Pastikan _isLoading false jika UID null
                _isLoading.value = false
                emit(emptyList())
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Fungsi untuk di-trigger dari UI (DaftarRuanganScreen) saat firebase UID sudah diketahui
    fun loadRuanganForUser(userFirebaseUid: String?) {
        _currentFirebaseUid.value = userFirebaseUid
    }

    // --- Fitur Detail Ruangan dan Perangkat (tetap seperti sebelumnya) ---
    private val _ruanganDetail = MutableStateFlow<RuanganWithPerangkat?>(null)
    val ruanganDetail: StateFlow<RuanganWithPerangkat?> = _ruanganDetail.asStateFlow()

    private val _namaRuangan = MutableStateFlow<String?>(null)
    val namaRuangan: StateFlow<String?> = _namaRuangan.asStateFlow()

    private val _lampuWithPerangkat = MutableStateFlow<List<LampWithPerangkat>>(emptyList())
    val lampuWithPerangkat: StateFlow<List<LampWithPerangkat>> = _lampuWithPerangkat.asStateFlow() // Gunakan asStateFlow untuk exposure

    fun loadLampuFor(ruanganId: Int) {
        viewModelScope.launch {
            // Tidak perlu set _isLoading di sini karena ini untuk detail, bukan list utama
            // kecuali Anda punya state loading terpisah untuk detail
            try {
                val pwList = rpRepository.getPerangkatWithWaktuByRuanganId(ruanganId)
                val combined = pwList.mapNotNull { pw ->
                    val lampu = rpRepository.getLampuByPerangkatId(pw.perangkatId)
                    val perangkat = rpRepository.getPerangkatById(pw.perangkatId)
                    if (lampu != null && perangkat != null) {
                        LampWithPerangkat(lampu, perangkat)
                    } else null
                }
                _lampuWithPerangkat.value = combined
            } catch (e: Exception) {
                // Handle error spesifik untuk loadLampuFor jika perlu
                _lampuWithPerangkat.value = emptyList()
            }
        }
    }

    fun loadNamaRuangan(ruanganId: Int) {
        viewModelScope.launch {
            try {
                // Asumsi getNamaRuangan mengembalikan Flow<String?>
                ruanganRepository.getNamaRuangan(ruanganId).collect { nama ->
                    _namaRuangan.value = nama
                }
            } catch (e: Exception) {
                _namaRuangan.value = null // Atau handle error sesuai kebutuhan
            }
        }
    }

    fun loadDetail(ruanganId: Int) {
        viewModelScope.launch {
            try {
                // Asumsi getRuanganWithPerangkat mengembalikan Flow<RuanganWithPerangkat?>
                ruanganRepository.getRuanganWithPerangkat(ruanganId)
                    .collect { detail ->
                        _ruanganDetail.value = detail
                    }
            } catch (e: Exception) {
                _ruanganDetail.value = null // Atau handle error
            }
        }
    }

    // --- Operasi CRUD Ruangan ---
    fun insertRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ruanganRepository.insertRuangan(ruangan)
                // Tidak perlu memuat ulang _currentFirebaseUid.value secara manual di sini
                // karena `allRuangan` akan otomatis re-fetch dari Flow jika database berubah
                // dan `ruanganRepository.getAllRuanganByFirebaseUid(uid)` adalah Flow dari Room.
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ruanganRepository.deleteRuangan(ruangan)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRuangan(ruangan: RuanganEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ruanganRepository.updateRuangan(ruangan)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}