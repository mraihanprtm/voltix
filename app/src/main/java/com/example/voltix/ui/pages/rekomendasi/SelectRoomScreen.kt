// SelectRoomScreen.kt
package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.viewmodel.simulasi.RuanganViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectRoomScreen(
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel()
) {
    // Observe rooms owned by user
    val ruanganList by ruanganViewModel.allRuangan.observeAsState(emptyList())
    var expanded by remember { mutableStateOf(false) }
    var selectedRuangan by remember { mutableStateOf<RuanganEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pilih Ruangan",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedRuangan?.namaRuangan ?: "-- Pilih --",
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
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedRuangan?.let { ru ->
                    // Navigate to rekomendasi screen with selectedId
                    navController.navigate("rekomendasi/${ru.id}")
                }
            },
            enabled = selectedRuangan != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lanjutkan")
        }
    }
}
