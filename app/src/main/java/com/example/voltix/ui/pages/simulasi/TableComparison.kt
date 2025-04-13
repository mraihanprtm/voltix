package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.viewmodel.PerangkatViewModel
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel
import kotlin.math.roundToInt

@Composable
fun TableComparison(viewModel: SimulasiViewModel, perangkatViewModel: PerangkatViewModel = hiltViewModel()) {
    val perangkatAsli = perangkatViewModel.perangkatList.value.orEmpty()
    viewModel.cloneDariPerangkatAsli(perangkatAsli)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Keterangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Sebelum", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Sesudah", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        Divider(color = Color.Black, thickness = 1.dp)

        // Data tabel
        val rows = listOf(
            "Total Daya (W)" to (viewModel.totalDayaSebelum to viewModel.totalDaya),
            "Total Konsumsi Harian (kWh)" to ("%.2f".format(viewModel.totalKonsumsiSebelum) to "%.2f".format(viewModel.totalKonsumsi)),
            "Estimasi Biaya Harian (Rp)" to ("Rp ${viewModel.totalBiayaSebelum.roundToInt()}" to "Rp ${viewModel.totalBiaya.roundToInt()}")
        )

        rows.forEach { (label, values) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(values.first.toString(), fontSize = 14.sp, modifier = Modifier.weight(1f))  // Sebelum
                Text(values.second.toString(), fontSize = 14.sp, modifier = Modifier.weight(1f)) // Sesudah
            }
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}