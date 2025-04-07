import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.PerangkatEntity
import com.example.voltix.data.SimulasiPerangkatEntity
import com.example.voltix.data.repository.SimulasiRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer
import com.example.voltix.data.entity.PerangkatListrikEntity
import com.google.android.libraries.places.api.model.LocalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SimulasiViewModel @Inject constructor(
    private val repository: SimulasiRepository
) : ViewModel() {


    val perangkatSimulasi = mutableStateListOf<SimulasiPerangkatEntity>()

    val semuaSimulasi: LiveData<List<SimulasiPerangkatEntity>> = repository.semuaSimulasi

    // Input
    var namaBaru by mutableStateOf("")
    var dayaBaru by mutableStateOf("")
    var kategoriBaru by mutableStateOf("")
    var waktuNyalaBaru by mutableStateOf("")
    var waktuMatiBaru by mutableStateOf("")

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


    // Cloning dari daftar perangkat asli
    var sudahDiClone = false

    fun cloneDariPerangkatAsli(asli: List<PerangkatListrikEntity>) {
        if (sudahDiClone) return  // ⛔️ Skip kalau sudah pernah cloning

        val cloned = asli.map {
            SimulasiPerangkatEntity(
                nama = it.nama,
                daya = it.daya,
                kategori = it.kategori,
                waktuNyala = it.waktuNyala,
                waktuMati = it.waktuMati
            )
        }


        // Hitung data sebelum
        totalDayaSebelum = asli.sumOf { it.daya }
//        totalKonsumsiSebelum = asli.sumOf { (it.daya * it.durasi).toDouble() } / 1000.0
        totalBiayaSebelum = totalKonsumsiSebelum * tarifListrik  // Pastikan tarifListrik punya nilai

        viewModelScope.launch {
            repository.clear()
            repository.tambahSemua(cloned)
            sudahDiClone = true
        }
    }



    fun tambahPerangkat() {
        val daya = dayaBaru.toIntOrNull() ?: return

        val baru = SimulasiPerangkatEntity(
            nama = namaBaru,
            daya = daya,
            kategori = kategoriBaru,
            waktuNyala = waktuNyalaBaru,
            waktuMati = waktuMatiBaru
        )

        viewModelScope.launch { repository.tambah(baru) }
        resetInput()
    }


    fun hapusPerangkat(p: SimulasiPerangkatEntity) {
        viewModelScope.launch { repository.hapus(p) }
    }

    fun editPerangkat(nama: String, daya: Int, kategori: String, waktuNyala: String, waktuMati: String) {
        perangkatDiedit?.let {
            val updated = it.copy(
                nama = nama,
                daya = daya,
                kategori = kategori,
                waktuNyala = waktuNyala,
                waktuMati = waktuMati
            )
            viewModelScope.launch { repository.update(updated) }
            perangkatDiedit = null
        }
        showEditDialog = false
    }


    fun resetInput() {
        namaBaru = ""
        dayaBaru = ""
        kategoriBaru = ""
        waktuNyalaBaru = ""
        waktuMatiBaru = ""
        showTambahDialog = false
    }


    // Perhitungan Total

    fun getDurasi(waktuNyala: String, waktuMati: String): Double {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            // Parse times
            val calStart = Calendar.getInstance()
            val calEnd = Calendar.getInstance()

            // Set both calendars to the same date to focus only on time
            calStart.time = format.parse(waktuNyala)
            calEnd.time = format.parse(waktuMati)

            // Get time in milliseconds
            var durationMillis = calEnd.timeInMillis - calStart.timeInMillis

            // If end time is earlier than start time, add 24 hours
            if (durationMillis < 0) {
                durationMillis += 24 * 60 * 60 * 1000 // 24 hours in milliseconds
            }

            // Convert milliseconds to hours
            durationMillis.toDouble() / (60 * 60 * 1000)
        } catch (e: Exception) {
            0.0
        }
    }


    val totalDaya: Int
        get() = perangkatSimulasi.sumOf { it.daya }

    val totalKonsumsi: Double
        get() = perangkatSimulasi.sumOf { (it.daya * getDurasi(it.waktuNyala, it.waktuMati)) / 1000.0 }

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
