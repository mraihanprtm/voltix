package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.voltix.ui.component.TimePickerDialogButton
import java.time.Duration
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel

@Composable
fun EditPerangkatDialog(viewModel: SimulasiViewModel) {
    val perangkat = viewModel.perangkatDiedit ?: return

    var nama by remember { mutableStateOf(perangkat.nama) }
    var daya by remember { mutableStateOf(perangkat.daya.toString()) }
    var waktuNyala by remember { mutableStateOf(perangkat.waktuNyala) }
    var waktuMati by remember { mutableStateOf(perangkat.waktuMati) }

    // Durasi otomatis berdasarkan waktu nyala dan mati
    val durasi = remember(waktuNyala, waktuMati) {
        val durasiJam = Duration.between(waktuNyala, waktuMati).toMinutes().toFloat() / 60f
        if (durasiJam < 0) 0f else durasiJam
    }

    AlertDialog(
        onDismissRequest = { viewModel.showEditDialog = false },
        title = { Text("Edit Perangkat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daya,
                    onValueChange = { daya = it },
                    label = { Text("Daya (Watt)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TimePickerDialogButton("Waktu Nyala", waktuNyala) { waktuNyala = it }
                TimePickerDialogButton("Waktu Mati", waktuMati) { waktuMati = it }

                Text("Durasi Otomatis: ${"%.2f".format(durasi)} jam", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dayaInt = daya.toIntOrNull() ?: return@Button
                    viewModel.editPerangkat(nama, dayaInt, waktuNyala, waktuMati, durasi)
                    viewModel.showEditDialog = false
                }
            ) {
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
