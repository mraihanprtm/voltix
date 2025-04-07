package com.example.voltix.ui.simulasi

import SimulasiViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.voltix.data.PerangkatEntity
import com.example.voltix.viewmodel.PerangkatViewModel
import androidx.compose.runtime.getValue

@Composable
fun DeviceList(viewModel: PerangkatViewModel) {
    val perangkatList by viewModel.perangkatList.observeAsState(emptyList()) // observe LiveData

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
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

            // Daftar Perangkat
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn {
                    items(perangkatList) { item -> // ← pakai list yang sudah di-observe
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.nama, modifier = Modifier.weight(1f))
                            Text("${item.daya} W", modifier = Modifier.weight(1f))
//                            Text("${item.durasi} Jam", modifier = Modifier.weight(1f))

                            Row {
                                IconButton(
                                    onClick = {

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
                                        viewModel.deletePerangkat(item)
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
}
