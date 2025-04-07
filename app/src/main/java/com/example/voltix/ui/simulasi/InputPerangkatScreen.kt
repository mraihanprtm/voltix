package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.KategoriPerangkat
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.viewmodel.PerangkatViewModel
import java.time.LocalTime
import java.time.Duration

@Composable
fun InputPerangkatScreen(
    navController: NavHostController,
    viewModel: PerangkatViewModel = hiltViewModel(),
    onPerangkatDisimpan: () -> Unit = {}
) {
    var nama by remember { mutableStateOf("") }
    var daya by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf(KategoriPerangkat.ELEKTRONIK) }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(18, 0)) }

    val durasi = remember(waktuNyala, waktuMati) {
        val durasiJam = Duration.between(waktuNyala, waktuMati).toMinutes().toFloat() / 60f
        if (durasiJam < 0) 0f else durasiJam
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DeviceList(viewModel = viewModel)

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

        // Dropdown kategori
        DropdownKategori(
            selectedKategori = kategori,
            onKategoriSelected = { kategori = it }
        )

        // Time picker untuk waktu nyala dan mati
        TimePickerDialogButton("Waktu Nyala", waktuNyala) { waktuNyala = it }
        TimePickerDialogButton("Waktu Mati", waktuMati) { waktuMati = it }

        // Tampilkan durasi hasil perhitungan
        Text("Durasi Otomatis: ${"%.2f".format(durasi)} jam", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                val dayaInt = daya.toIntOrNull() ?: 0
                if (nama.isNotBlank() && dayaInt > 0 && durasi > 0f) {
                    viewModel.insertPerangkat(nama, dayaInt, kategori, waktuNyala, waktuMati, durasi)
                    nama = ""
                    daya = ""
                    kategori = KategoriPerangkat.ELEKTRONIK
                    waktuNyala = LocalTime.of(6, 0)
                    waktuMati = LocalTime.of(18, 0)
                    onPerangkatDisimpan()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nama.isNotBlank() && daya.isNotBlank()
        ) {
            Text("Simpan Perangkat")
        }
    }
}



