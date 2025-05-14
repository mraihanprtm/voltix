// RekomendasiScreen.kt
package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    ruanganId: Int,
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    // Load detail & lampu-perangkat combo when ruanganId changes
    LaunchedEffect(ruanganId) {
        ruanganViewModel.loadDetail(ruanganId)
        ruanganViewModel.loadLampuFor(ruanganId)
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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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
            // Detail Ruangan
            detail?.let { d ->
                Text(text = "Nama Ruangan: ${d.ruangan.namaRuangan}")
                Text(text = "Jenis Ruangan: ${d.ruangan.jenisRuangan}")
                Text(text = "Luas: ${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²")
            } ?: Text("Memuat detail ruangan...")

            Spacer(modifier = Modifier.height(12.dp))

            // Informasi Lampu + Perangkat
            if (lampuWithPerangkat.isEmpty()) {
                Text(text = "Belum ada lampu di ruangan ini.")
            } else {
                // mengambil entri pertama
                val item: LampWithPerangkat = lampuWithPerangkat.first()
                LampuWithPerangkatInfo(item)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        detail?.let { d ->
                            val input = LampRecommendationInput(
                                jenisRuangan = d.ruangan.jenisRuangan,
                                panjang = d.ruangan.panjangRuangan,
                                lebar = d.ruangan.lebarRuangan,
                                lampOutputLm = item.lumenTotal,
                                lampEfficacy = item.lumenPerLamp
                            )
                            rekomViewModel.calculateAndSave(
                                input = input,
                                userId = null,
                                ruanganId = ruanganId,
                                lampuId = item.lampu.id
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hitung Rekomendasi")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading, Error, dan Hasil
            when {
                loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                error != null -> Text(
                    text = error ?: "Terjadi kesalahan",
                    color = MaterialTheme.colorScheme.error
                )
                result != null -> RecommendationResult(result = result!!)
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