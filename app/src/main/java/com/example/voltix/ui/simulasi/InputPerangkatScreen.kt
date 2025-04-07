package com.example.voltix.ui.simulasi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voltix.ui.component.TimePickerField
import com.example.voltix.viewmodel.PerangkatViewModel

@Composable
fun InputPerangkatScreen(
    navController: NavHostController,
    viewModel: PerangkatViewModel = hiltViewModel(),
    onPerangkatDisimpan: () -> Unit = {}
) {
    var nama by remember { mutableStateOf("") }
    var daya by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var waktuNyala by remember { mutableStateOf("") }
    var waktuMati by remember { mutableStateOf("") }

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

        TimePickerField(
            label = "Jam Perangkat Mulai Digunakan",
            timeText = waktuNyala,
            onTimeSelected = { waktuNyala = it }
        )

        TimePickerField(
            label = "Jam Perangkat Selesai Digunakan",
            timeText = waktuMati,
            onTimeSelected = { waktuMati = it }
        )

        OutlinedTextField(
            value = kategori,
            onValueChange = { kategori = it },
            label = { Text("Kategori perangkat") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )



        Button(
            onClick = {
                val dayaInt = daya.toIntOrNull() ?: 0
//                val durasiFloat = durasi.toFloatOrNull() ?: 0f

                if (nama.isNotBlank() && dayaInt > 0 && waktuNyala.isNotBlank() && waktuMati.isNotBlank() && kategori.isNotBlank()) {
                    viewModel.insertPerangkat(nama, dayaInt, kategori, waktuNyala, waktuMati)
                    // Reset input
                    nama = ""
                    daya = ""
                    kategori = ""
                    waktuNyala = ""
                    waktuMati = ""
                    onPerangkatDisimpan()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nama.isNotBlank() && daya.isNotBlank() && kategori.isNotBlank() && waktuNyala.isNotBlank() && waktuMati.isNotBlank()
        ) {
            Text("Simpan Perangkat")
        }
    }
}
