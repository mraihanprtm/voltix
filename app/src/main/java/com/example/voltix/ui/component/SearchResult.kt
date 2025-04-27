package com.example.voltix.ui.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltix.data.entity.ElectronicInformationModel

@Composable
fun SearchResultItem(
    data: ElectronicInformationModel,
    onItemClick: (deviceType: String, wattage: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onItemClick(
                    data.deviceType ?: "",
                    data.wattage ?: ""
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tambahkan log untuk debugging
            Log.d("SearchResultItem", "Title: ${data.title}")
            Log.d("SearchResultItem", "DeviceType: ${data.deviceType}")
            Log.d("SearchResultItem", "Wattage: ${data.wattage}")

            Text(text = data.title ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = data.displayedLink ?: "", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            // Tampilkan informasi device type dan wattage
            if (!data.deviceType.isNullOrEmpty() || !data.wattage.isNullOrEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (!data.deviceType.isNullOrEmpty()) {
                            Text(
                                text = "Jenis: ${data.deviceType}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (!data.wattage.isNullOrEmpty()) {
                            Text(
                                text = "Daya: ${data.wattage}W",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Text(text = data.snippet ?: "", fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}
