package com.example.voltix.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.voltix.data.entity.jenis  // Import enum jenis
import java.lang.reflect.Modifier

@Composable
fun DropdownKategori(
    selectedJenis: jenis,  // Ubah parameter ke jenis
    onJenisSelected: (jenis) -> Unit  // Ubah callback ke jenis
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = when(selectedJenis) {  // Tambahkan formatting yang lebih baik
                jenis.Lampu -> "Lampu"
                jenis.Lainnya -> "Lainnya"
            })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            jenis.values().forEach { jenisItem ->  // Gunakan values() dari enum jenis
                DropdownMenuItem(
                    text = {
                        Text(when(jenisItem) {  // Format text yang ditampilkan
                            jenis.Lampu -> "Lampu"
                            jenis.Lainnya -> "Lainnya"
                        })
                    },
                    onClick = {
                        onJenisSelected(jenisItem)
                        expanded = false
                    }
                )
            }
        }
    }
}