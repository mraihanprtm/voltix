package com.example.voltix.ui.pages.rekomendasi

import RekomendasiViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiPenghematanLampu(
    navController: NavHostController,
    viewModel: RekomendasiViewModel = hiltViewModel()
) {
    val rekomendasiList = viewModel.uiState.rekomendasi

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Rekomendasi") })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (rekomendasiList.isEmpty()) {
                Text("Belum ada rekomendasi. Silakan jalankan simulasi dulu.")
            } else {
                rekomendasiList.forEach { item ->
                    Card(modifier = Modifier.padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = item.perangkatName)
                            Text(
                                text = "Potensi Hemat: ${item.potensiHemat} kWh/bln",
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}