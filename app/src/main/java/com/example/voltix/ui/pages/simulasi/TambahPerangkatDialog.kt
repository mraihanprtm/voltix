package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.hitungDurasi
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
                    onValueChange = { newValue ->
                        // Only allow digits, a single decimal point, and prevent multiple decimal points
                        val filteredValue = newValue.replace(Regex("[^0-9.]"), "")
                            .replace(Regex("\\.(?=.*\\.)"), "")

                        // Validate that it can be parsed as a Double if not empty
                        if (filteredValue.isEmpty() || filteredValue.toDoubleOrNull() != null) {
                            viewModel.dayaBaru = filteredValue
                        }
                    },
                    label = { Text("Daya (Watt)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                TimePickerDialogButton("Waktu Nyala", viewModel.waktuNyalaBaru) { viewModel.waktuNyalaBaru = it }
                TimePickerDialogButton("Waktu Mati", viewModel.waktuMatiBaru) { viewModel.waktuMatiBaru = it }

                // Durasi otomatis berdasarkan waktu nyala dan mati
                val durasi = remember(viewModel.waktuNyalaBaru, viewModel.waktuMatiBaru) {
                    hitungDurasi(viewModel.waktuNyalaBaru, viewModel.waktuMatiBaru)
                }
                Text(text = "Durasi: ${durasi} jam")

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
