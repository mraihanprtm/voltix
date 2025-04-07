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
import dagger.hilt.android.lifecycle.HiltViewModel
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
    var durasiBaru by mutableStateOf("")

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

    fun cloneDariPerangkatAsli(asli: List<PerangkatListrEntity>) {
        if (sudahDiClone) return  // ⛔️ Skip kalau sudah pernah cloning

        val cloned = asli.map {
            SimulasiPerangkatEntity(nama = it.nama, daya = it.daya, durasi = it.durasi)
        }

        // Hitung data sebelum
        totalDayaSebelum = asli.sumOf { it.daya }
        totalKonsumsiSebelum = asli.sumOf { (it.daya * it.durasi).toDouble() } / 1000.0
        totalBiayaSebelum = totalKonsumsiSebelum * tarifListrik  // Pastikan tarifListrik punya nilai

        viewModelScope.launch {
            repository.clear()
            repository.tambahSemua(cloned)
            sudahDiClone = true
        }
    }



    fun tambahPerangkat() {
        val daya = dayaBaru.toIntOrNull() ?: return
        val durasi = durasiBaru.toFloatOrNull() ?: return
        val baru = SimulasiPerangkatEntity(nama = namaBaru, daya = daya, durasi = durasi)
        viewModelScope.launch { repository.tambah(baru) }
        resetInput()
    }

    fun hapusPerangkat(p: SimulasiPerangkatEntity) {
        viewModelScope.launch { repository.hapus(p) }
    }

    fun editPerangkat(nama: String, daya: Int, durasi: Float) {
        perangkatDiedit?.let {
            val updated = it.copy(nama = nama, daya = daya, durasi = durasi)
            viewModelScope.launch { repository.update(updated) }
            perangkatDiedit = null
        }
        showEditDialog = false
    }

    fun resetInput() {
        namaBaru = ""
        dayaBaru = ""
        durasiBaru = ""
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
