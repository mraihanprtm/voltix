package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@Composable
fun TableSimulasiRentang(viewModel: SimulasiViewModel = hiltViewModel(), rentang: String,
                         jumlahPeriode: String, onValueChange: (String, String) -> Unit) {
    var selectedRentang by remember { mutableStateOf("Harian") }
    var jumlahPeriode by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Simulasi Berdasarkan Rentang Waktu:", fontWeight = FontWeight.Bold)
        // Dropdown pilihan rentang waktu
        Text("Pilih Rentang Waktu:", fontWeight = FontWeight.Bold)
        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth() // Modifier harus berada di sini
            ) {
                Text(selectedRentang)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Harian", "Mingguan", "Bulanan").forEach { rentang ->
                    DropdownMenuItem(
                        text = { Text(rentang) },
                        onClick = {
                            selectedRentang = rentang
                            expanded = false
                        }
                    )
                }
            }
        }

        // Input jumlah periode
        Text("Jumlah Periode ($selectedRentang):", fontWeight = FontWeight.Bold)
        TextField(
            value = jumlahPeriode,
            onValueChange = { jumlahPeriode = it.filter { char -> char.isDigit() } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Hitung faktor berdasarkan rentang waktu
        val faktor = when (selectedRentang) {
            "Mingguan" -> jumlahPeriode.toIntOrNull()?.times(7) ?: 1
            "Bulanan" -> jumlahPeriode.toIntOrNull()?.times(30) ?: 1
            else -> jumlahPeriode.toIntOrNull() ?: 1
        }

        val totalDaya = viewModel.totalDaya * faktor
        val totalKonsumsi = viewModel.totalKonsumsi * faktor
        val totalBiaya = viewModel.totalBiaya * faktor

        Spacer(modifier = Modifier.height(16.dp))

        // Tampilkan hasil simulasi dalam tabel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Keterangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Hasil Simulasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(color = Color.Black, thickness = 1.dp)

            val rows = listOf(
                "Total Daya (W)" to totalDaya,
                "Total Konsumsi (kWh)" to "%.2f".format(totalKonsumsi),
                "Estimasi Biaya (Rp)" to "Rp ${totalBiaya.roundToInt()}"
            )

            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(value.toString(), fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }
    }
}