//package com.example.voltix.ui.simulasi
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.voltix.viewmodel.simulasi.SimulasiViewModel
//
//@Composable
//fun SimulasiPerangkatList(
//    simulasiId: Int,
//    viewModel: SimulasiViewModel = viewModel()
//) {
//    // Observasi data perangkat dengan detailnya
//    val perangkatList = viewModel.simulasiPerangkatWithDetail.collectAsState().value
//
//    // Memuat perangkat ketika pertama kali
//    if (perangkatList.isEmpty()) {
//        viewModel.loadPerangkatForSimulasi(simulasiId)
//    }
//
//    // Menampilkan perangkat dengan detailnya
//    Column(modifier = Modifier.padding(16.dp)) {
//        perangkatList.forEach { perangkat ->
//            Text("Nama Perangkat: ${perangkat.namaPerangkat}")
//            Text("Daya: ${perangkat.daya}W")
//            Text("Jumlah: ${perangkat.jumlah}")
//            Text("Waktu Nyala: ${perangkat.waktuNyala} jam")
//            Text("Waktu Mati: ${perangkat.waktuMati} jam")
//        }
//    }
//}
