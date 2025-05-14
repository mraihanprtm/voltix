package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val ruanganList by ruanganViewModel.allRuangan.observeAsState(initial = emptyList())
    var selectedRuanganId by remember { mutableIntStateOf(ruanganId) }
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(ruanganList) {
        if (ruanganList.isNotEmpty() && ruanganList.none { it.id == selectedRuanganId }) {
            selectedRuanganId = ruanganList.first().id
            textFieldValue = ruanganList.first().namaRuangan
            scope.launch { rekomViewModel.resetResult() }
        }
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
        ) {
            Text("Pilih Ruangan:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it; expanded = true },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("Pilih Ruangan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ruanganList.filter { it.namaRuangan.contains(textFieldValue, ignoreCase = true) }
                        .forEach { ruangan ->
                            DropdownMenuItem(text = { Text(ruangan.namaRuangan) }, onClick = {
                                selectedRuanganId = ruangan.id
                                textFieldValue = ruangan.namaRuangan
                                expanded = false
                                scope.launch { rekomViewModel.resetResult() }
                            })
                        }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            detail?.let { d ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nama Ruangan: ${d.ruangan.namaRuangan}")
                        Text("Jenis Ruangan: ${d.ruangan.jenisRuangan}")
                        Text("Luas: ${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²")
                    }
                }
            } ?: run {
                if (ruanganList.isEmpty()) Text("Belum ada ruangan. Tambahkan terlebih dahulu.")
                else Text("Memuat detail ruangan...")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (lampuWithPerangkat.isEmpty()) {
                Text("Belum ada lampu di ruangan ini.", style = MaterialTheme.typography.bodyLarge)
            } else {
                val item = lampuWithPerangkat.first()
                val area = detail!!.ruangan.panjangRuangan * detail!!.ruangan.lebarRuangan

                val currentQty = item.jumlah
                val currentTotalLumen = item.lumenTotal
                val currentTotalPower = currentQty * item.dayaPerLamp
                val currentDensity = currentTotalPower / area

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Informasi Lampu Saat Ini", style = MaterialTheme.typography.titleMedium)
                        Text("• Jumlah lampu: $currentQty")
                        Text("• Total lumen: $currentTotalLumen lm")
                        Text("• Total daya: ${"%.2f".format(currentTotalPower)} W")
                        Text("• Densitas: ${"%.2f".format(currentDensity)} W/m²")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Informasi Lampu Pinjaman", style = MaterialTheme.typography.titleMedium)
                        LampuWithPerangkatInfo(item)
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            detail?.let { d ->
                                val input = LampRecommendationInput(
                                    jenisRuangan = d.ruangan.jenisRuangan,
                                    panjang = d.ruangan.panjangRuangan,
                                    lebar = d.ruangan.lebarRuangan,
                                    lampOutputLm = item.lumenPerLamp,
                                    lampEfficacy = (item.lumenPerLamp / item.dayaPerLamp).toInt()
                                )
                                rekomViewModel.calculateAndSave(input, null, selectedRuanganId, item.lampu.id)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Hitung Rekomendasi")
                        }

                        result?.let { r ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                Text("Perbandingan Saat Ini vs Rekomendasi", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Perbaikan untuk bagian perbandingan
                                ComparisonTable(
                                    currentQty = currentQty,
                                    currentTotalLumen = currentTotalLumen,
                                    currentTotalPower = currentTotalPower,
                                    currentDensity = currentDensity,
                                    recommendedResult = r
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Text(error ?: "Terjadi kesalahan", color = MaterialTheme.colorScheme.error)
                result != null -> Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hasil Rekomendasi:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        RecommendationResult(result = result!!)
                    }
                }
            }

            // Add extra space at bottom to ensure all content is visible with scrolling
            Spacer(modifier = Modifier.height(32.dp))
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
        Text(text = "Efficacy: ${item.lampPowerWatt.toInt()} W per lampu")
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

@Composable
fun ComparisonTable(
    currentQty: Int,
    currentTotalLumen: Int,
    currentTotalPower: Double,
    currentDensity: Double,
    recommendedResult: LampRecommendationResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kategori",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Saat Ini",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Rekomendasi",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Content rows
            ComparisonTableRow(
                label = "Jumlah lampu",
                current = currentQty.toString(),
                recommended = recommendedResult.numberOfLamps.toString()
            )

            ComparisonTableRow(
                label = "Total lumen",
                current = "$currentTotalLumen lm",
                recommended = "${recommendedResult.totalLumen} lm"
            )

            ComparisonTableRow(
                label = "Total daya",
                current = "%.2f W".format(currentTotalPower),
                recommended = "%.2f W".format(recommendedResult.totalPowerWatt)
            )

            ComparisonTableRow(
                label = "Densitas daya",
                current = "%.2f W/m²".format(currentDensity),
                recommended = "%.2f W/m²".format(recommendedResult.densityPower)
            )

            // Status SNI hanya ada di rekomendasi
            ComparisonTableRow(
                label = "Status SNI",
                current = "-",
                recommended = if (recommendedResult.withinStandard) "✅ Sesuai" else "❌ Melebihi"
            )
        }
    }
}

@Composable
fun ComparisonTableRow(
    label: String,
    current: String,
    recommended: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = current,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = recommended,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}