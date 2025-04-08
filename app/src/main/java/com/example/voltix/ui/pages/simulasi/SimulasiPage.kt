package com.example.voltix.ui.simulasi

import PeringatanMelebihiDaya
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voltix.data.repository.SimulasiRepository
import com.example.voltix.viewmodel.PerangkatViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel

@Composable
fun SimulasiPage(
    navController: NavHostController,
    viewModel: SimulasiViewModel = hiltViewModel(),
    perangkatViewModel: PerangkatViewModel = hiltViewModel()// ✅ Tambahkan ini,
) {
    val context = LocalContext.current
    val perangkatAsli by perangkatViewModel.perangkatList.observeAsState(emptyList())

    LaunchedEffect(perangkatAsli) {
        if (!viewModel.sudahDiClone) {
            viewModel.cloneDariPerangkatAsli(perangkatAsli)
        }
    }

    // State untuk menyimpan rentang waktu dan jumlah periode
    var rentang by remember { mutableStateOf("Harian") }
    var jumlahPeriode by remember { mutableStateOf("1") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("SIMULASI KONSUMSI LISTRIK", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
                // Device List Title
                item {
                    Text("Daftar Perangkat Elektronik:", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                }

                // Device List
                item {
                    SimulasiDeviceList(viewModel = viewModel)
                }

                item {
                    Button(
                        onClick = {
                            viewModel.sudahDiClone = false // supaya bisa clone lagi
                            val perangkatAsli = perangkatViewModel.perangkatList.value.orEmpty()
                            viewModel.cloneDariPerangkatAsli(perangkatAsli)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Simulasi")
                    }
                }
                item {
                    Button(
                        onClick = {
//                            viewModel.simpanKonfigurasiSimulasi() // Buat fungsi ini kalau belum ada
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simpan Simulasi")
                    }
                }



                // Add Device Button
                item {
                    Button(
                        onClick = { viewModel.showTambahDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tambah Perangkat Baru")
                    }
                }

                // Simulasi Konsumsi Listrik Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tabel Perbandingan Sebelum dan Sesuah", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TableComparison(viewModel)
                        }
                    }
                }

                // Hasil Selisih
                item {
                    HasilSelisih(viewModel)
                }

                // TableSimulasiRentang dengan callback untuk memperbarui state
                item {
                    TableSimulasiRentang(viewModel, rentang, jumlahPeriode) { newRentang, newJumlahPeriode ->
                        rentang = newRentang
                        jumlahPeriode = newJumlahPeriode
                    }
                }

                // Download PDF Button
                item {
                    Button(
                        onClick = { generatePdf(context, viewModel, rentang, jumlahPeriode.toIntOrNull() ?: 1) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Laporan PDF")
                    }
                }
            }

            // Dialogs and Warnings
            if (viewModel.showEditDialog) {
                EditPerangkatDialog(viewModel)
            }

            if (viewModel.showTambahDialog) {
                TambahPerangkatDialog(viewModel)
            }

            if (viewModel.melebihiDaya) {
                PeringatanMelebihiDaya(viewModel)
            }
        }
    )
}