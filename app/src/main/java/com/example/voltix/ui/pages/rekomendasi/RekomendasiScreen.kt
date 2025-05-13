package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.LampuEntity
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import com.example.voltix.viewmodel.simulasi.RuanganViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    val ruanganList by ruanganViewModel.allRuangan.observeAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    var selectedRuangan by remember { mutableStateOf<RuanganEntity?>(null) }
    val detail by ruanganViewModel.ruanganDetail.collectAsState(initial = null)
    val loading by rekomViewModel.loading.observeAsState(initial = false)
    val error by rekomViewModel.error.observeAsState(initial = null)
    val result by rekomViewModel.result.observeAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedRuangan?.namaRuangan ?: "Pilih Ruangan",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ruangan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ruanganList.forEach { ru ->
                    DropdownMenuItem(
                        text = { Text(ru.namaRuangan) },
                        onClick = {
                            selectedRuangan = ru
                            expanded = false
                            ruanganViewModel.loadDetail(ru.id)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        selectedRuangan?.let { ru ->
            Text(text = "Jenis Ruangan: ${ru.jenisRuangan}")
            Text(text = "Luas: ${ru.panjangRuangan} x ${ru.lebarRuangan} m²")
            detail?.lampuList?.firstOrNull()?.let { lamp: LampuEntity ->
                Text(text = "Jenis Lampu: ${lamp.jenis}")
                Text(text = "Jumlah: ${lamp.jumlah}")
                Text(text = "Lumen: ${lamp.lumen}")
                Text(text = "Efficacy: ${lamp.lumen / lamp.jumlah} lm/W")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedRuangan?.let { ru ->
                    detail?.lampuList?.firstOrNull()?.let { lamp: LampuEntity ->
                        val input = LampRecommendationInput(
                            jenisRuangan = ru.jenisRuangan,
                            panjang = ru.panjangRuangan,
                            lebar = ru.lebarRuangan,
                            lampOutputLm = lamp.lumen,
                            lampEfficacy = lamp.lumen / lamp.jumlah
                        )
                        rekomViewModel.calculateAndSave(
                            input = input,
                            userId = null,
                            ruanganId = ru.id,
                            lampuId = lamp.id
                        )
                    }
                }
            },
            enabled = selectedRuangan != null && (detail?.lampuList?.isNotEmpty() == true),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rekomendasi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        result?.let { res ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Jumlah Lampu: ${res.numberOfLamps}")
            Text("Total Lumen: ${res.totalLumen}")
            Text("Total Daya: ${"%.2f".format(res.totalPowerWatt)} W")
            Text("Densitas Daya: ${"%.2f".format(res.densityPower)} W/m²")
            Text(if (res.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI")
        }
    }
}