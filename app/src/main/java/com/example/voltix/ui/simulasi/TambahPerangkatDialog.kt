package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TambahPerangkatDialog(viewModel: SimulasiViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.showTambahDialog = false },
        title = { Text("Tambah Perangkat Baru") },
        text = {
            Column {
                OutlinedTextField(value = viewModel.namaBaru, onValueChange = { viewModel.namaBaru = it }, label = { Text("Nama Perangkat") })
                OutlinedTextField(value = viewModel.dayaBaru, onValueChange = { viewModel.dayaBaru = it }, label = { Text("Daya (Watt)") })
                OutlinedTextField(value = viewModel.durasiBaru, onValueChange = { viewModel.durasiBaru = it }, label = { Text("Durasi (Jam)") })
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.tambahPerangkat() }) {
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