package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    ruanganId: Int,
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    // Observasi daftar ruangan dari LiveData
    val ruanganList by ruanganViewModel.allRuangan.observeAsState(initial = emptyList())
    // State untuk ruangan yang dipilih
    var selectedRuanganId by remember { mutableStateOf(ruanganId) }
    // State untuk text field dropdown
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Reset selected id jika tidak ada di list (misal setelah hapus)
    LaunchedEffect(ruanganList) {
        if (ruanganList.isNotEmpty()) {
            if (ruanganList.none { it.id == selectedRuanganId }) {
                selectedRuanganId = ruanganList.first().id
                textFieldValue = ruanganList.first().namaRuangan
                scope.launch { rekomViewModel.resetResult() }
            }
        }
    }

    // Load detail & lampu ketika seleksi berubah
    LaunchedEffect(selectedRuanganId) {
        ruanganViewModel.loadDetail(selectedRuanganId)
        ruanganViewModel.loadLampuFor(selectedRuanganId)
    }

    val detail by ruanganViewModel.ruanganDetail.collectAsState(initial = null)
    val lampuWithPerangkat by ruanganViewModel.lampuWithPerangkat.collectAsState(initial = emptyList())
    val loading by rekomViewModel.loading.observeAsState(initial = false)
    val error by rekomViewModel.error.observeAsState(initial = null)
    val result by rekomViewModel.result.observeAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Lampu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Pilih Ruangan:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        expanded = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Pilih Ruangan") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    ruanganList
                        .filter { it.namaRuangan.contains(textFieldValue, ignoreCase = true) }
                        .forEach { ruangan ->
                            DropdownMenuItem(
                                text = { Text(ruangan.namaRuangan) },
                                onClick = {
                                    selectedRuanganId = ruangan.id
                                    textFieldValue = ruangan.namaRuangan
                                    expanded = false
                                    scope.launch { rekomViewModel.resetResult() }
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detail Ruangan
            detail?.let { d ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nama Ruangan: ${d.ruangan.namaRuangan}")
                        Text(text = "Jenis Ruangan: ${d.ruangan.jenisRuangan}")
                        Text(text = "Luas: ${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²")
                    }
                }
            } ?: run {
                if (ruanganList.isEmpty()) {
                    Text("Belum ada ruangan. Silakan tambahkan ruangan terlebih dahulu.")
                } else {
                    Text("Memuat detail ruangan...")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informasi Lampu + Perangkat
            if (lampuWithPerangkat.isEmpty()) {
                Text(text = "Belum ada lampu di ruangan ini.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informasi Lampu:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val item = lampuWithPerangkat.first()
                        LampuWithPerangkatInfo(item)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            detail?.let { d ->
                                val input = LampRecommendationInput(
                                    jenisRuangan = d.ruangan.jenisRuangan,
                                    panjang = d.ruangan.panjangRuangan,
                                    lebar = d.ruangan.lebarRuangan,
                                    lampOutputLm = item.lumenTotal,
                                    lampEfficacy = item.lumenPerLamp
                                )
                                rekomViewModel.calculateAndSave(input, null, selectedRuanganId, item.lampu.id)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Hitung Rekomendasi")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading, Error, dan Hasil
            when {
                loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Text(text = error ?: "Terjadi kesalahan", color = MaterialTheme.colorScheme.error)
                result != null -> Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hasil Rekomendasi:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        RecommendationResult(result = result!!)
                    }
                }
            }
        }
    }
}

@Composable
private fun LampuWithPerangkatInfo(item: LampWithPerangkat) {
    Column {
        Text(text = "Jenis Lampu: ${item.lampu.jenis}")
        Text(text = "Jumlah: ${item.jumlah}")
        Text(text = "Total Lumen: ${item.lumenTotal}")
        Text(text = "Lumen per Lampu: ${item.lumenPerLamp} lm")
        Text(text = "Efficacy: ${item.lumenPerLamp} lm/W")
    }
}

@Composable
private fun RecommendationResult(result: LampRecommendationResult) {
    Column {
        Text(text = "Jumlah Lampu: ${result.numberOfLamps}")
        Text(text = "Total Lumen: ${result.totalLumen}")
        Text(text = "Total Daya: ${"%.2f".format(result.totalPowerWatt)} W")
        Text(text = "Densitas Daya: ${"%.2f".format(result.densityPower)} W/m²")
        Text(text = if (result.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI")
    }
}