package com.example.voltix.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun InputBarangScreen(
    navController: NavHostController,
//    viewModel: DataViewModel = hiltViewModel(),
) {
    var namaPerangkat by remember { mutableStateOf("") }
    var daya by remember { mutableStateOf("") }
    var mulaiPenggunaan by remember { mutableStateOf("") }
    var selesaiPenggunaan by remember { mutableStateOf("") }
    var selectedPenggunaan by remember { mutableStateOf("") }
    val penggunaanOptions = listOf("Setiap Hari", "Hanya Weekend", "Hari Kerja")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Tambah", fontSize = 18.sp)
        Text("Perangkat", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = namaPerangkat,
            onValueChange = { namaPerangkat = it },
            label = { Text("Nama Perangkat") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = daya,
            onValueChange = { daya = it },
            label = { Text("Daya Listrik Perangkat") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = mulaiPenggunaan,
                onValueChange = { mulaiPenggunaan = it },
                label = { Text("Mulai Penggunaan") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = selesaiPenggunaan,
                onValueChange = { selesaiPenggunaan = it },
                label = { Text("Selesai Penggunaan") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DropdownField(
            label = "Penggunaan",
            options = penggunaanOptions,
            selectedOption = selectedPenggunaan,
            onOptionSelected = { selectedPenggunaan = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* handle tambah */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAFF63)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Tambah", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Add, contentDescription = "Tambah")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* handle kembali */ },
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFF1D6))
        ) {
            Text("Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Close, contentDescription = "Kembali")
        }

        Spacer(modifier = Modifier.height(24.dp))

        BottomNavigationBar()
    }
}

@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    BottomNavigation(
        backgroundColor = Color.White
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("HOME") },
            selected = false,
            onClick = { }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Devices, contentDescription = "Devices") },
            label = { Text("DEVICES") },
            selected = true,
            onClick = { }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Recap") },
            label = { Text("RECAP") },
            selected = false,
            onClick = { }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("PROFILE") },
            selected = false,
            onClick = { }
        )
    }
}
