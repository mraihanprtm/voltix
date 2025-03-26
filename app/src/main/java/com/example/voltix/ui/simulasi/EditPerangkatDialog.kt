package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EditPerangkatDialog(viewModel: SimulasiViewModel) {
    val perangkat = viewModel.perangkatDiedit ?: return
    var nama by remember { mutableStateOf(perangkat.nama) }
    var daya by remember { mutableStateOf(perangkat.daya.toString()) }
    var durasi by remember { mutableStateOf(perangkat.durasi.toString()) }

    AlertDialog(
        onDismissRequest = { viewModel.showEditDialog = false },
        title = { Text("Edit Perangkat") },
        text = {
            Column {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                OutlinedTextField(value = daya, onValueChange = { daya = it }, label = { Text("Daya (Watt)") })
                OutlinedTextField(value = durasi, onValueChange = { durasi = it }, label = { Text("Durasi (Jam)") })
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.editPerangkat(nama, daya.toIntOrNull() ?: perangkat.daya, durasi.toFloatOrNull() ?: perangkat.durasi) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.showEditDialog = false }) {
                Text("Batal")
            }
        }
    )
}