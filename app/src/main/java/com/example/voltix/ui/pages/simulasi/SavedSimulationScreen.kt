package com.example.voltix.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voltix.data.entity.SimulationWithDevices
import com.example.voltix.ui.Screen
import com.example.voltix.ui.Screen.SavedSimulations.route
import com.example.voltix.ui.viewmodel.SimulationComparisonViewModel

@Composable
fun SavedSimulationsScreen(
    navController: NavController,
    viewModel: SimulationComparisonViewModel = hiltViewModel()
) {
    val simulations by viewModel.simulations.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopBar()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
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
                    onSelect = { simulation ->
                        val route =  Screen.SimulasiBebas.createRoute(simulation.simulation.id)
                        navController.navigate(route)
                    },
                    onCompare = { selectedSimulations ->
                        val ids = selectedSimulations.joinToString(",") { it.simulation.id.toString() }
                        navController.navigate(Screen.SimulationComparison.createRoute(ids))
                    }
                )
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
            text = "Simulasi Tersimpan",
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
    onSelect: (SimulationWithDevices) -> Unit,
    onCompare: (List<SimulationWithDevices>) -> Unit
) {
    val selectedSimulations = remember { mutableStateListOf<SimulationWithDevices>() }

    Column {
        if (selectedSimulations.isNotEmpty()) {
            Button(
                onClick = { onCompare(selectedSimulations.toList()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Bandingkan (${selectedSimulations.size})", color = Color.White)
            }
        }
        LazyColumn {
            items(simulations) { simulation ->
                SimulationCard(
                    simulation = simulation,
                    isSelected = simulation in selectedSimulations,
                    onSelect = {
                        onSelect(simulation)
                    },
                    onToggleSelection = {
                        if (simulation in selectedSimulations) {
                            selectedSimulations.remove(simulation)
                        } else {
                            selectedSimulations.add(simulation)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SimulationCard(
    simulation: SimulationWithDevices,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
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
            Column {
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