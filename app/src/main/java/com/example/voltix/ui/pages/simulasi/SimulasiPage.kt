package com.example.voltix.ui.simulasi

import PeringatanMelebihiDaya
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.voltix.viewmodel.PerangkatViewModel
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulasiPage(
    navController: NavHostController,
    viewModel: SimulasiViewModel = hiltViewModel(),
    perangkatViewModel: PerangkatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val perangkatAsli by perangkatViewModel.perangkatList.observeAsState(emptyList())
    var showDeviceList by remember { mutableStateOf(true) }
    var showSimulationResults by remember { mutableStateOf(true) }
    var showDetailedSimulation by remember { mutableStateOf(true) }
    // State untuk menyimpan rentang waktu dan jumlah periode
    var rentang by remember { mutableStateOf("Harian") }
    var jumlahPeriode by remember { mutableStateOf("1") }

    LaunchedEffect(perangkatAsli) {
        if (!viewModel.sudahDiClone) {
            viewModel.cloneDariPerangkatAsli(perangkatAsli)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Simulasi Konsumsi Listrik",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 Simulasi Energi",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SummaryItem(
                                title = "Total Daya",
                                value = "${viewModel.totalDaya} w",
                                icon = "🔌"
                            )

                            SummaryItem(
                                title = "Total Konsumsi",
                                value = "${"%.2f".format(viewModel.totalKonsumsi)} Kwh",
                                icon = "⚡"
                            )

                            SummaryItem(
                                title = "Est. Biaya",
                                value = "Rp ${"%,.0f".format(viewModel.totalBiaya)}",
                                icon = "💰"
                            )
                        }
                    }
                }
            }

            // Device Management Section
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    onClick = { showDeviceList = !showDeviceList }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔌 Daftar Perangkat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (showDeviceList) "🔼" else "🔽",
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Device List (Collapsible)
            item {
                AnimatedVisibility(
                    visible = showDeviceList,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Device list
                        SimulasiDeviceList(viewModel = viewModel)

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.sudahDiClone = false
                                    val perangkatAsli = perangkatViewModel.perangkatList.value.orEmpty()
                                    viewModel.cloneDariPerangkatAsli(perangkatAsli)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("🔄 Reset")
                            }

                            Button(
                                onClick = { viewModel.showTambahDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("➕ Tambah")
                            }
                        }
                    }
                }
            }

            // Results Section
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    onClick = { showSimulationResults = !showSimulationResults }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📈 Hasil Simulasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (showSimulationResults) "🔼" else "🔽",
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Results Content (Collapsible)
            item {
                AnimatedVisibility(
                    visible = showSimulationResults,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Perbandingan Sebelum & Sesudah",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TableComparison(viewModel)

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            HasilSelisih(viewModel)
                        }
                    }
                }
            }

            // Detailed Simulation Section
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    onClick = { showDetailedSimulation = !showDetailedSimulation }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ Simulasi Detail",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (showDetailedSimulation) "🔼" else "🔽",
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Detailed Simulation Content (Collapsible)
            item {
                AnimatedVisibility(
                    visible = showDetailedSimulation,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // TableSimulasiRentang with callback
                            TableSimulasiRentang(viewModel, rentang, jumlahPeriode) { newRentang, newJumlahPeriode ->
                                rentang = newRentang
                                jumlahPeriode = newJumlahPeriode
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { generatePdf(context, viewModel, rentang, jumlahPeriode.toIntOrNull() ?: 1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("📋 Download PDF")
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
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
}

@Composable
fun SummaryItem(title: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}