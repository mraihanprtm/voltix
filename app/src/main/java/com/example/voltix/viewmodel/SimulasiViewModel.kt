import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.voltix.ui.Perangkat
import com.example.voltix.ui.samplePerangkat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SimulasiViewModel: ViewModel() {
    var dayaMaksimum by mutableStateOf(1300) // Daya listrik rumah (Watt)
    var tarifListrik by mutableStateOf(1444.7) // Tarif listrik per kWh

    // ✅ GUNAKAN mutableStateListOf() AGAR TIDAK ADA MASALAH DENGAN SCROLLING
    var perangkat = mutableStateListOf<Perangkat>()
        private set

    var namaBaru by mutableStateOf("")
    var dayaBaru by mutableStateOf("")
    var durasiBaru by mutableStateOf("")
    var perangkatDiedit by mutableStateOf<Perangkat?>(null)
    var showEditDialog by mutableStateOf(false)
    var showTambahDialog by mutableStateOf(false)

    var totalDayaSebelum by mutableStateOf(0)
    var totalKonsumsiSebelum by mutableStateOf(0.0)
    var totalBiayaSebelum by mutableStateOf(0.0)

    val totalDaya: Int get() = perangkat.sumOf { it.daya }
    val totalKonsumsi: Double get() = perangkat.sumOf { (it.daya * it.durasi) / 1000.0 }
    val totalBiaya: Double get() = totalKonsumsi * tarifListrik
    val melebihiDaya: Boolean get() = totalDaya > dayaMaksimum

    val selisihDaya: Int get() = totalDaya - totalDayaSebelum
    val selisihKonsumsi: Double get() = totalKonsumsi - totalKonsumsiSebelum
    val selisihBiaya: Double get() = totalBiaya - totalBiayaSebelum

    init {
        perangkat.addAll(samplePerangkat) // ✅ PASTIKAN DATA AWAL DIISI
        simpanDataSebelum() // Simpan data awal agar "Sebelum" tidak 0
    }

    fun simpanDataSebelum() {
        totalDayaSebelum = totalDaya
        totalKonsumsiSebelum = totalKonsumsi
        totalBiayaSebelum = totalBiaya
    }

    fun tambahPerangkat() {
        simpanDataSebelum()
        val daya = dayaBaru.toIntOrNull() ?: 0
        val durasi = durasiBaru.toFloatOrNull() ?: 0f
        if (namaBaru.isNotEmpty() && daya > 0 && durasi > 0) {
            perangkat.add(Perangkat(namaBaru, daya, durasi)) // ✅ GUNAKAN add()
            namaBaru = ""
            dayaBaru = ""
            durasiBaru = ""
        }
        showTambahDialog = false
    }

    fun hapusPerangkat(perangkatDihapus: Perangkat) {
        simpanDataSebelum()
        perangkat.remove(perangkatDihapus) // ✅ GUNAKAN remove()
    }

    fun editPerangkat(nama: String, daya: Int, durasi: Float) {
        simpanDataSebelum()
        perangkatDiedit?.let { perangkatLama ->
            val index = perangkat.indexOf(perangkatLama)
            if (index != -1) {
                perangkat[index] = Perangkat(nama, daya, durasi) // ✅ GANTI LANGSUNG DALAM LIST
            }
        }
        perangkatDiedit = null
        showEditDialog = false
    }
}
