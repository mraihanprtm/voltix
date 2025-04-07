package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TambahPerangkatDialog(viewModel: SimulasiViewModel = hiltViewModel()) {
    AlertDialog(
        onDismissRequest = { viewModel.showTambahDialog = false },
        title = { Text("Tambah Perangkat Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = viewModel.namaBaru,
                    onValueChange = { viewModel.namaBaru = it },
                    label = { Text("Nama Perangkat") }
                )
                OutlinedTextField(
                    value = viewModel.dayaBaru,
                    onValueChange = { viewModel.dayaBaru = it },
                    label = { Text("Daya (Watt)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = viewModel.kategoriBaru,
                    onValueChange = { viewModel.kategoriBaru = it },
                    label = { Text("Kategori") }
                )
                OutlinedTextField(
                    value = viewModel.waktuNyalaBaru,
                    onValueChange = { viewModel.waktuNyalaBaru = it },
                    label = { Text("Waktu Nyala (HH:mm)") }
                )
                OutlinedTextField(
                    value = viewModel.waktuMatiBaru,
                    onValueChange = { viewModel.waktuMatiBaru = it },
                    label = { Text("Waktu Mati (HH:mm)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.tambahPerangkat()
                viewModel.showTambahDialog = false
            }) {
                Text("Tambah")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.showTambahDialog = false }) {
                Text("Batal")
            }
        }
    )
}
