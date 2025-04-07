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
import com.example.voltix.data.entity.KategoriPerangkat

@Composable
fun DropdownKategori(
    selectedKategori: KategoriPerangkat,
    onKategoriSelected: (KategoriPerangkat) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = selectedKategori.name)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            KategoriPerangkat.values().forEach { kategori ->
                DropdownMenuItem(
                    text = { Text(kategori.name) },
                    onClick = {
                        onKategoriSelected(kategori)
                        expanded = false
                    }
                )
            }
        }
    }
}
