package com.example.voltix.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voltix.ui.theme.VoltixTheme

@Composable
fun SimulasiScreen(
    onSimulasiBebasClick: () -> Unit,
    onSimulasiBerdasarkanRuanganClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pilih Mode Simulasi",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onSimulasiBebasClick) {
            Text("Simulasi Bebas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onSimulasiBerdasarkanRuanganClick) {
            Text("Simulasi Berdasarkan Ruangan")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SimulasiScreenPreview() {
    VoltixTheme {
        SimulasiScreen(
            onSimulasiBebasClick = {},
            onSimulasiBerdasarkanRuanganClick = {}
        )
    }
}
