package com.example.voltix.ui.simulasi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voltix.viewmodel.PerangkatViewModel

@Composable
fun InputPerangkatScreen(
    navController: NavHostController,
    viewModel: PerangkatViewModel = viewModel(),
    onPerangkatDisimpan: () -> Unit = {}
) {
    var nama by remember { mutableStateOf("") }
    var daya by remember { mutableStateOf("") }
    var durasi by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Komponen daftar perangkat yang sudah kamu buat
        DeviceList(viewModel = viewModel)

        // Form input
        Text(text = "Tambah Perangkat", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Perangkat") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = daya,
            onValueChange = { daya = it },
            label = { Text("Daya (Watt)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = durasi,
            onValueChange = { durasi = it },
            label = { Text("Durasi (Jam per hari)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val dayaInt = daya.toIntOrNull() ?: 0
                val durasiFloat = durasi.toFloatOrNull() ?: 0f

                if (nama.isNotBlank() && dayaInt > 0 && durasiFloat > 0f) {
                    viewModel.insertPerangkat(nama, dayaInt, durasiFloat)
                    // Reset input
                    nama = ""
                    daya = ""
                    durasi = ""
                    onPerangkatDisimpan()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nama.isNotBlank() && daya.isNotBlank() && durasi.isNotBlank()
        ) {
            Text("Simpan Perangkat")
        }
    }
}
