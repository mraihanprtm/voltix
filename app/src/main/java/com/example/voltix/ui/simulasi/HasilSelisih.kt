package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@Composable
fun HasilSelisih(viewModel: SimulasiViewModel = hiltViewModel()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF508CD5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Selisih Dengan Penggunaan Sebelumnya",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Tabel perbandingan
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Keterangan", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Perubahan", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                Divider(color = Color.White, thickness = 1.dp)

                // Data tabel
                val rows = listOf(
                    "Total Daya (W)" to viewModel.selisihDaya,
                    "Total Konsumsi Harian (kWh)" to viewModel.selisihKonsumsi,
                    "Estimasi Biaya Harian (Rp)" to viewModel.selisihBiaya
                )

                rows.forEach { (label, value) ->
                    val formattedValue = when (value) {
                        is Int -> value.toString() // Daya
                        is Double -> "%.2f".format(value) // Konsumsi harian
                        else -> "Rp ${(value as Double).roundToInt()}" // Biaya
                    }

                    val textColor = when {
                        (value as Number).toDouble() < 0 -> Color.Green // Jika ada penghematan
                        else -> Color.White
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = Color.White, modifier = Modifier.weight(1f))
                        Text(formattedValue, color = textColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                }

                Divider(color = Color.White, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Menampilkan pesan hemat jika ada pengurangan pada daya, konsumsi, atau biaya
            val hematDaya = viewModel.selisihDaya < 0
            val hematKonsumsi = viewModel.selisihKonsumsi < 0
            val hematBiaya = viewModel.selisihBiaya < 0

            if (hematDaya || hematKonsumsi || hematBiaya) {
                Text(
                    "ANDA MENGHEMAT:",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (hematDaya) Text("💡 Daya: ${-viewModel.selisihDaya} W", color = Color.Green)
                if (hematKonsumsi) Text("⚡ Konsumsi: %.2f kWh".format(-viewModel.selisihKonsumsi), color = Color.Green)
                if (hematBiaya) Text("💰 Biaya: Rp ${-viewModel.selisihBiaya.roundToInt()}", color = Color.Green)
            }
        }
    }
}