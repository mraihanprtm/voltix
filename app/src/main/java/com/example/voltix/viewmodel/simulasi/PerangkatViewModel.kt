package com.example.voltix.viewmodel.simulasi

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.RuanganPerangkatCrossRef
import com.example.voltix.data.entity.UserEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.data.repository.RuanganAndPerangkatRepository
import com.example.voltix.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class PerangkatViewModel @Inject constructor(
    private val repository: RuanganAndPerangkatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _perangkatList = MutableStateFlow<List<PerangkatEntity>>(emptyList())
    val perangkatList = _perangkatList.asStateFlow()

    private val _jenisListrik = MutableStateFlow(0)
    val jenisListrik: StateFlow<Int> = _jenisListrik

    private val _perangkatByRuangan = MutableLiveData<List<PerangkatEntity>>()
    val perangkatByRuangan: LiveData<List<PerangkatEntity>> = _perangkatByRuangan

    private val _totalDaya = MutableStateFlow(0)
    val totalDaya: StateFlow<Int> = _totalDaya.asStateFlow()

    private val _totalKonsumsi = MutableStateFlow(0.0)
    val totalKonsumsi: StateFlow<Double> = _totalKonsumsi.asStateFlow()

    private val _totalBiaya = MutableStateFlow(0.0)
    val totalBiaya: StateFlow<Double> = _totalBiaya.asStateFlow()

    private val _melebihiDaya = MutableStateFlow(false)
    val melebihiDaya: StateFlow<Boolean> = _melebihiDaya.asStateFlow()

    private var batasDayaPengguna: Int = 0

    var perangkatDiedit by mutableStateOf<PerangkatEntity?>(null)
    var showEditDialog by mutableStateOf(false)

    init {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("PerangkatViewModel", "Current User ID: $userId")

                if (userId != null) {
                    val currentUser = userRepository.getUserByUid(userId)

                    // Log user data
                    Log.d("PerangkatViewModel", "Current User Data: $currentUser")

                    if (currentUser != null) {
                        _jenisListrik.value = currentUser.jenisListrik
                        batasDayaPengguna = currentUser.jenisListrik

                        // Log the values
                        Log.d("PerangkatViewModel", "Jenis Listrik set to: ${_jenisListrik.value}")
                        Log.d("PerangkatViewModel", "Batas Daya set to: $batasDayaPengguna")
                    } else {
                        Log.w("PerangkatViewModel", "User data not found for ID: $userId")
                    }
                } else {
                    Log.w("PerangkatViewModel", "No user is currently logged in")
                }
            } catch (e: Exception) {
                Log.e("PerangkatViewModel", "Error loading user data", e)
            }
        }
    }

    fun loadPerangkatByRuangan(ruanganId: Int) {
        currentRuanganId = ruanganId
        viewModelScope.launch {
            try {
                val list = repository.getPerangkatByRuanganId(ruanganId)
                _perangkatList.value = list
                _perangkatByRuangan.postValue(list)

                // Ambil data perangkat dengan waktu
                val perangkatWithWaktu = repository.getPerangkatWithWaktuByRuanganId(ruanganId)

                // Hitung durasi untuk setiap perangkat
                val durasiMap = mutableMapOf<Int, Int>()
                perangkatWithWaktu.forEach { pwt ->
                    val durasi = calculateDurasi(pwt.waktuNyala, pwt.waktuMati)
                    durasiMap[pwt.perangkatId] = durasi

                    // Log untuk debugging
                    Log.d("PerangkatViewModel",
                        "Perangkat ${pwt.nama}: " +
                                "waktuNyala=${pwt.waktuNyala}, " +
                                "waktuMati=${pwt.waktuMati}, " +
                                "durasi=$durasi"
                    )
                }
                _durasiPenggunaan.value = durasiMap

                // Update total daya dan flag melebihi
                val total = list.sumOf { it.daya * it.jumlah }
                _totalDaya.value = total
                _melebihiDaya.value = total > batasDayaPengguna

                // Hitung total konsumsi (daya * durasi * jumlah)
                val totalKonsumsiValue = list.sumOf { perangkat ->
                    val durasi = durasiMap[perangkat.id] ?: 0
                    (perangkat.daya * durasi * perangkat.jumlah).toDouble()
                }
                _totalKonsumsi.value = totalKonsumsiValue / 1000.0 // konversi ke kWh

                // Hitung total biaya (asumsi Rp 1.445 per kWh)
                _totalBiaya.value = _totalKonsumsi.value * 1445.0

            } catch (e: Exception) {
                Log.e("PerangkatViewModel", "Error loading perangkat", e)
            }
        }
    }

    fun insertPerangkatToRuangan(
        nama: String,
        jumlah: Int,
        daya: Int,
        kategori: jenis,
        waktuNyala: LocalTime,
        waktuMati: LocalTime,
        ruanganId: Int
    ) {
        viewModelScope.launch {
            try {
                val perangkat = PerangkatEntity(
                    nama = nama,
                    jumlah = jumlah,
                    daya = daya,
                    jenis = kategori
                )
                val perangkatId = repository.insertAndGetId(perangkat).toInt()

                val crossRef = RuanganPerangkatCrossRef(
                    ruanganId = ruanganId,
                    perangkatId = perangkatId,
                    waktuNyala = waktuNyala,
                    waktuMati = waktuMati
                )
                repository.insertCrossRef(crossRef)

                // Hitung dan update durasi untuk perangkat baru
                val durasi = calculateDurasi(waktuNyala, waktuMati)
                _durasiPenggunaan.value = _durasiPenggunaan.value + (perangkatId to durasi)

                // Log untuk debugging
                Log.d("PerangkatViewModel", "Perangkat baru $nama: waktuNyala=$waktuNyala, waktuMati=$waktuMati, durasi=$durasi")

                loadPerangkatByRuangan(ruanganId)
            } catch (e: Exception) {
                Log.e("PerangkatViewModel", "Error inserting perangkat", e)
            }
        }
    }

    fun editPerangkat(
        nama: String,
        jumlah: Int,
        daya: Int,
        kategori: jenis,
        waktuNyala: LocalTime,
        waktuMati: LocalTime
    ) {
        viewModelScope.launch {
            try {
                perangkatDiedit?.let { currentPerangkat ->
                    // Update perangkat
                    val updatedPerangkat = currentPerangkat.copy(
                        nama = nama,
                        daya = daya,
                        jenis = kategori,
                        jumlah = jumlah
                    )
                    repository.updatePerangkat(updatedPerangkat)

                    // Update cross ref dengan waktu baru
                    currentRuanganId?.let { ruanganId ->
                        val updatedCrossRef = RuanganPerangkatCrossRef(
                            ruanganId = ruanganId,
                            perangkatId = currentPerangkat.id,
                            waktuNyala = waktuNyala,
                            waktuMati = waktuMati
                        )
                        repository.updateCrossRef(updatedCrossRef)

                        // Hitung durasi baru
                        val durasi = calculateDurasi(waktuNyala, waktuMati)
                        _durasiPenggunaan.value = _durasiPenggunaan.value + (currentPerangkat.id to durasi)

                        // Log untuk debugging
                        Log.d("PerangkatViewModel",
                            "Edit perangkat ${currentPerangkat.nama}: " +
                                    "waktuNyala=$waktuNyala, " +
                                    "waktuMati=$waktuMati, " +
                                    "durasi=$durasi"
                        )

                        loadPerangkatByRuangan(ruanganId)
                    }

                    perangkatDiedit = null
                    showEditDialog = false
                }
            } catch (e: Exception) {
                Log.e("PerangkatViewModel", "Error editing perangkat", e)
            }
        }
    }

    private var currentRuanganId: Int? = null

    fun deletePerangkat(perangkat: PerangkatEntity) {
        viewModelScope.launch {
            try {
                currentRuanganId?.let { ruanganId ->
                    repository.deleteCrossRef(
                        RuanganPerangkatCrossRef(
                            ruanganId = ruanganId,
                            perangkatId = perangkat.id,
                            waktuNyala = LocalTime.MIDNIGHT,
                            waktuMati = LocalTime.MIDNIGHT
                        )
                    )
                }
                repository.deletePerangkat(perangkat)
                currentRuanganId?.let { loadPerangkatByRuangan(it) }
            } catch (e: Exception) {}
        }
    }

    private fun updateTotalDaya(perangkatList: List<PerangkatEntity>) {
        val total = perangkatList.sumOf { perangkat ->
            perangkat.daya * perangkat.jumlah
        }
        _totalDaya.value = total
    }

    private fun checkMelebihiDaya() {
        _melebihiDaya.value = _totalDaya.value > batasDayaPengguna
    }

    private val _durasiPenggunaan = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val durasiPenggunaan: StateFlow<Map<Int, Int>> = _durasiPenggunaan.asStateFlow()

    fun getDurasiPenggunaan(perangkatId: Int): Int {
        return _durasiPenggunaan.value[perangkatId] ?: 0
    }

    private fun calculateDurasi(start: LocalTime, end: LocalTime): Int {
        return if (end.isAfter(start) || end == start) {
            Duration.between(start, end).toHours().toInt()
        } else {
            // Jika waktu mati lebih awal dari waktu nyala, berarti melewati tengah malam
            val tillMidnight = Duration.between(start, LocalTime.MAX).toHours()
            val fromMidnight = Duration.between(LocalTime.MIN, end).toHours()
            (tillMidnight + fromMidnight).toInt()
        }
    }

    // PerangkatViewModel.kt (tambahan method)
    fun updateJenisListrik(newJenisListrik: Int) {
        viewModelScope.launch {
            try {
                _jenisListrik.value = newJenisListrik
                batasDayaPengguna = newJenisListrik
                checkMelebihiDaya()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}