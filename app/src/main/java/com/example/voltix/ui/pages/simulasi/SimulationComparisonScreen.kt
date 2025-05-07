package com.example.voltix.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.style.TextOverflow
import com.example.voltix.data.entity.SimulationWithDevices
import com.example.voltix.ui.viewmodel.ComparisonResult
import com.example.voltix.ui.viewmodel.SimulationComparisonViewModel
import java.time.Duration
import java.time.LocalTime
import kotlinx.coroutines.launch

@Composable
fun SimulationComparisonScreen(
    viewModel: SimulationComparisonViewModel = hiltViewModel()
) {
    val simulations by viewModel.simulations.observeAsState(initial = emptyList())
    val comparisonResults by viewModel.comparisonResults.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val selectedSimulations = remember { mutableStateListOf<SimulationWithDevices>() }
    var showComparison by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAllSimulations()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedSimulations.size >= 2) {
                        viewModel.compareSimulations(selectedSimulations)
                        showComparison = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Pilih setidaknya 2 simulasi untuk membandingkan")
                        }
                    }
                },
                containerColor = if (selectedSimulations.size >= 2) Color(0xFF3F51B5) else Color(0xFFB0BEC5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Build, contentDescription = "Bandingkan")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp) // Avoid FAB overlap
                .verticalScroll(rememberScrollState()), // Keep vertical scroll
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (simulations.isEmpty()) {
                Text(
                    text = "Belum ada simulasi tersimpan.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                SimulationList(
                    simulations = simulations,
                    selectedSimulations = selectedSimulations,
                    onToggleSelection = { simulation ->
                        if (simulation in selectedSimulations) {
                            selectedSimulations.remove(simulation)
                        } else {
                            selectedSimulations.add(simulation)
                        }
                        showComparison = false // Reset comparison when selection changes
                    }
                )
                AnimatedVisibility(visible = showComparison && comparisonResults.isNotEmpty()) {
                    ComparisonTable(comparisonResults = comparisonResults)
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Perbandingan Simulasi",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                fontSize = 28.sp
            )
        )
    }
}

@Composable
private fun SimulationList(
    simulations: List<SimulationWithDevices>,
    selectedSimulations: List<SimulationWithDevices>,
    onToggleSelection: (SimulationWithDevices) -> Unit
) {
    Column {
        simulations.forEach { simulation ->
            SimulationCard(
                simulation = simulation,
                isSelected = simulation in selectedSimulations,
                onToggleSelection = { onToggleSelection(simulation) }
            )
        }
    }
}

@Composable
private fun SimulationCard(
    simulation: SimulationWithDevices,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) Color(0xFF3F51B5) else Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleSelection() }
            ) {
                Text(
                    text = simulation.simulation.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A237E)
                    )
                )
                Text(
                    text = "Perangkat: ${simulation.devices.size}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
                Text(
                    text = "Daya: ${"%.2f".format(simulation.calculatePowerUsage())} kWh",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
                Text(
                    text = "Biaya: Rp ${"%.2f".format(simulation.calculateCost())}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3F51B5)
                )
            )
        }
    }
}

@Composable
private fun ComparisonTable(comparisonResults: List<ComparisonResult>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Hasil Perbandingan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F51B5))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TableCell(text = "Simulasi", weight = 0.3f, color = Color.White)
                    TableCell(text = "Daya (kWh)", weight = 0.2f, color = Color.White)
                    TableCell(text = "Biaya (Rp)", weight = 0.2f, color = Color.White)
                    TableCell(text = "Hemat Daya", weight = 0.2f, color = Color.White)
                    TableCell(text = "Hemat Biaya", weight = 0.2f, color = Color.White)
                }
                // Table Rows
                comparisonResults.forEachIndexed { index, result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) Color(0xFFF5F7FA) else Color(0xFFECEFF1))
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell(text = result.simulationName, weight = 0.3f)
                        TableCell(text = "%.2f".format(result.powerUsage), weight = 0.2f)
                        TableCell(text = "%.2f".format(result.cost), weight = 0.2f)
                        TableCell(
                            text = if (result.powerSavings == 0.0) "Terbaik" else "+%.2f".format(result.powerSavings),
                            weight = 0.2f,
                            color = if (result.powerSavings == 0.0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        )
                        TableCell(
                            text = if (result.costSavings == 0.0) "Terbaik" else "+%.2f".format(result.costSavings),
                            weight = 0.2f,
                            color = if (result.costSavings == 0.0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, weight: Float, color: Color = Color(0xFF424242)) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = color
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun SimulationWithDevices.calculatePowerUsage(): Double {
    return devices.sumOf { device ->
        val durationHours = if (device.waktuMati.isAfter(device.waktuNyala)) {
            Duration.between(device.waktuNyala, device.waktuMati).toHours().toDouble()
        } else {
            // Handle overnight usage
            Duration.between(device.waktuNyala, LocalTime.MAX).toHours().toDouble() +
                    Duration.between(LocalTime.MIN, device.waktuMati).toHours().toDouble()
        }
        (device.daya * device.jumlah * durationHours) / 1000.0 // Convert Wh to kWh
    }
}

private fun SimulationWithDevices.calculateCost(): Double {
    val costPerKWh = 1444.70 // Rp/kWh
    return calculatePowerUsage() * costPerKWh
}