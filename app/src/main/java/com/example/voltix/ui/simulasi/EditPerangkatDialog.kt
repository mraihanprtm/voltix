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
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EditPerangkatDialog(viewModel: SimulasiViewModel = hiltViewModel()) {
    val perangkat = viewModel.perangkatDiedit ?: return
    var nama by remember { mutableStateOf(perangkat.nama) }
    var daya by remember { mutableStateOf(perangkat.daya.toString()) }
    var kategori by remember { mutableStateOf(perangkat.kategori.toString()) }
    var waktuNyala by remember { mutableStateOf(perangkat.waktuNyala.toString()) }
    var waktuMati by remember { mutableStateOf(perangkat.waktuMati.toString()) }

    AlertDialog(
        onDismissRequest = { viewModel.showEditDialog = false },
        title = { Text("Edit Perangkat") },
        text = {
            Column {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                OutlinedTextField(value = daya, onValueChange = { daya = it }, label = { Text("Daya (Watt)") })
                OutlinedTextField(value = kategori, onValueChange = { kategori = it }, label = { Text("Kategori") })
                OutlinedTextField(value = waktuNyala, onValueChange = { waktuNyala = it }, label = { Text("Waktu Nyala") })
                OutlinedTextField(value = waktuMati, onValueChange = { waktuMati = it }, label = { Text("Waktu Mati") })
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.editPerangkat(nama, daya.toIntOrNull() ?: perangkat.daya, kategori ?: perangkat.kategori, waktuNyala ?: perangkat.waktuNyala, waktuMati ?: perangkat.waktuMati) }) {
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