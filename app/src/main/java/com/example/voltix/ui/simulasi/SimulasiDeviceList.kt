package com.example.voltix.ui.simulasi


import SimulasiViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SimulasiDeviceList(viewModel: SimulasiViewModel = hiltViewModel()) {
    val perangkatList = viewModel.perangkatSimulasi // langsung ambil dari mutableStateListOf

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Nama", modifier = Modifier.weight(1f))
                Text("Daya (W)", modifier = Modifier.weight(1f))
                Text("Jam Pakai", modifier = Modifier.weight(1f))
                Text("", modifier = Modifier.weight(1f))
            }
            Divider(color = Color.Black, thickness = 1.dp)

            // Daftar perangkat simulasi
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(perangkatList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.nama, modifier = Modifier.weight(1f))
                        Text("${item.daya} W", modifier = Modifier.weight(1f))
//                        Text("${item.durasi} Jam", modifier = Modifier.weight(1f))

                        Row {
                            IconButton(
                                onClick = {
                                    viewModel.perangkatDiedit = item
                                    viewModel.showEditDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painterResource(id = android.R.drawable.ic_menu_edit),
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.hapusPerangkat(item)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painterResource(id = android.R.drawable.ic_menu_delete),
                                    contentDescription = "Hapus",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

