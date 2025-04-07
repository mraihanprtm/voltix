package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel
import java.time.LocalTime

@Composable
fun TambahPerangkatDialog(viewModel: SimulasiViewModel) {
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
                    label = { Text("Daya (Watt)") }
                )
                OutlinedTextField(
                    value = viewModel.waktuNyalaBaru.toString(), // konversi LocalTime ke String
                    onValueChange = {
                        try {
                            viewModel.waktuNyalaBaru = LocalTime.parse(it)
                        } catch (_: Exception) {}
                    },
                    label = { Text("Waktu Nyala (HH:mm)") }
                )
                OutlinedTextField(
                    value = viewModel.waktuMatiBaru.toString(),
                    onValueChange = {
                        try {
                            viewModel.waktuMatiBaru = LocalTime.parse(it)
                        } catch (_: Exception) {}
                    },
                    label = { Text("Waktu Mati (HH:mm)") }
                )

                Text(text = "Durasi: ${viewModel.durasiBaru} jam")

                // Dropdown Kategori
                DropdownKategori (
                    selectedKategori = viewModel.kategoriBaru,
                    onKategoriSelected = { viewModel.kategoriBaru = it }
                )
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
