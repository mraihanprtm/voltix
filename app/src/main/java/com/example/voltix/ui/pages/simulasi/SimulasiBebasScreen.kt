package com.example.voltix.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.ui.theme.VoltixTheme

@Composable
fun SimulasiBebasScreen(
    perangkatList: List<PerangkatEntity>,
    onPerangkatSelect: (PerangkatEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Simulasi Bebas", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(perangkatList) { perangkat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(perangkat.nama)
                    Text("${perangkat.daya} W")
                    Button(onClick = { onPerangkatSelect(perangkat) }) {
                        Text("Pilih")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimulasiBebasScreenPreview() {
    VoltixTheme {
        SimulasiBebasScreen(
            perangkatList = listOf(
                PerangkatEntity(id = 1, nama = "Lampu", jumlah = 1, jenis = jenis.Lainnya, daya = 60),
                PerangkatEntity(id = 2, nama = "AC", jumlah = 1, jenis = jenis.Lainnya, daya = 1500)
            ),
            onPerangkatSelect = {}
        )
    }
}
