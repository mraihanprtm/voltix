package com.example.voltix.viewmodel.googlelens.SimulasiViewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.SimulasiPerangkatEntity
import com.example.voltix.data.repository.SimulasiRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer
import androidx.room.TypeConverter
import com.example.voltix.data.entity.KategoriPerangkat
import com.example.voltix.data.entity.PerangkatListrikEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject


import androidx.lifecycle.ViewModel



@RequiresApi(Build.VERSION_CODES.O)

@HiltViewModel
class SimulasiViewModel @Inject constructor(
    private val repository: SimulasiRepository
) : ViewModel() {

    val perangkatSimulasi = mutableStateListOf<SimulasiPerangkatEntity>()

    val semuaSimulasi: LiveData<List<SimulasiPerangkatEntity>> = repository.semuaSimulasi

    // Input
    var namaBaru by mutableStateOf("")
    var dayaBaru by mutableStateOf("")
    var kategoriBaru by mutableStateOf(KategoriPerangkat.OPSIONAL)
    var waktuNyalaBaru by mutableStateOf(LocalTime.of(6, 0))
    var waktuMatiBaru by mutableStateOf(LocalTime.of(18, 0))
    val durasiBaru: Float
        get() = hitungDurasi()

    var showEditDialog by mutableStateOf(false)
    var showTambahDialog by mutableStateOf(false)
    var perangkatDiedit by mutableStateOf<SimulasiPerangkatEntity?>(null)

    // Nilai Sebelum Simulasi
    var totalDayaSebelum by mutableStateOf(0)
    var totalKonsumsiSebelum by mutableStateOf(0.0)
    var totalBiayaSebelum by mutableStateOf(0.0)

    // Konfigurasi Simulasi
    var dayaMaksimum by mutableStateOf(1300)
    var tarifListrik by mutableStateOf(1444.7)

    // Observer untuk LiveData
    private val observer = Observer<List<SimulasiPerangkatEntity>> { list ->
        perangkatSimulasi.clear()
        perangkatSimulasi.addAll(list)
    }

    init {
        semuaSimulasi.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        semuaSimulasi.removeObserver(observer)
    }

    fun hitungDurasi(): Float {
        val durasiMenit = java.time.Duration.between(waktuNyalaBaru, waktuMatiBaru).toMinutes()
        return if (durasiMenit > 0) durasiMenit / 60f else 0f
    }


    class Converters {
        @TypeConverter
        fun fromLocalTime(value: LocalTime?): String? = value?.toString()

        @RequiresApi(Build.VERSION_CODES.O)
        @TypeConverter
        fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
    }

    // Cloning dari daftar perangkat asli
    var sudahDiClone = false

    fun cloneDariPerangkatAsli(asli: List<PerangkatListrikEntity>) {
        if (sudahDiClone) return

        val cloned = asli.map {
            SimulasiPerangkatEntity(
                nama = it.nama,
                daya = it.daya,
                kategori = it.kategori,
                durasi = it.durasi.toFloat(),
                waktuNyala = it.waktuNyala,
                waktuMati = it.waktuMati
            )
        }

        totalDayaSebelum = asli.sumOf { it.daya }
        totalKonsumsiSebelum = asli.sumOf { (it.daya * it.durasi).toDouble() } / 1000.0
        totalBiayaSebelum = totalKonsumsiSebelum * tarifListrik

        viewModelScope.launch {
            repository.clear()
            repository.tambahSemua(cloned)
            sudahDiClone = true
        }
    }

    fun tambahPerangkat() {
        val daya = dayaBaru.toIntOrNull() ?: return
        val durasi = hitungDurasi()
        val baru = SimulasiPerangkatEntity(
            nama = namaBaru,
            daya = daya,
            kategori = kategoriBaru,
            waktuNyala = waktuNyalaBaru,
            waktuMati = waktuMatiBaru,
            durasi = durasi,
        )
        viewModelScope.launch { repository.tambah(baru) }
        resetInput()
    }


    fun hapusPerangkat(p: SimulasiPerangkatEntity) {
        viewModelScope.launch { repository.hapus(p) }
    }



    fun editPerangkat(
        nama: String,
        daya: Int,
        waktuNyala: LocalTime,
        waktuMati: LocalTime,
        durasi: Float
    ) {
        perangkatDiedit?.let {
            val updated = it.copy(
                nama = nama,
                daya = daya,
                kategori = kategoriBaru,
                waktuNyala = waktuNyalaBaru,
                waktuMati = waktuMatiBaru,
                durasi = hitungDurasi()
            )
            viewModelScope.launch { repository.update(updated) }
            perangkatDiedit = null
        }
        showEditDialog = false
    }

    fun resetInput() {
        namaBaru = ""
        dayaBaru = ""
        kategoriBaru = KategoriPerangkat.OPSIONAL
        waktuNyalaBaru = LocalTime.of(6, 0)
        waktuMatiBaru = LocalTime.of(18, 0)
        showTambahDialog = false
    }


    // Perhitungan Total
    val totalDaya: Int
        get() = perangkatSimulasi.sumOf { it.daya }

    val totalKonsumsi: Double
        get() = perangkatSimulasi.sumOf { (it.daya * it.durasi) / 1000.0 }

    val totalBiaya: Double
        get() = totalKonsumsi * tarifListrik

    val melebihiDaya: Boolean
        get() = totalDaya > dayaMaksimum

    // Perhitungan Selisih
    val selisihDaya: Int
        get() = totalDaya - totalDayaSebelum

    val selisihKonsumsi: Double
        get() = totalKonsumsi - totalKonsumsiSebelum

    val selisihBiaya: Double
        get() = totalBiaya - totalBiayaSebelum

}
