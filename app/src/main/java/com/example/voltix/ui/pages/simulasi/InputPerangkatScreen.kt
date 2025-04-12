package com.example.voltix.ui.simulasi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.KategoriPerangkat
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.viewmodel.PerangkatViewModel
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.hitungDurasi
import java.time.LocalTime
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputPerangkatScreen(
    navController: NavHostController,
    viewModel: PerangkatViewModel = hiltViewModel(),
    onPerangkatDisimpan: () -> Unit = {}
) {
    var nama by remember { mutableStateOf("") }
    var daya by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf(KategoriPerangkat.OPSIONAL) }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var showDeviceList by remember { mutableStateOf(true) }
    var isFormValid by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val durasi = remember(waktuNyala, waktuMati) {
        hitungDurasi(waktuNyala, waktuMati)
    }

    // Validate form on content change
    LaunchedEffect(nama, daya, durasi) {
        isFormValid = nama.isNotBlank() && daya.isNotBlank() &&
                daya.toDoubleOrNull() != null && daya.toDoubleOrNull()!! > 0 &&
                durasi > 0f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Data Perangkat",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle for device list
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                onClick = { showDeviceList = !showDeviceList }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showDeviceList) "Sembunyikan Daftar Perangkat" else "Tampilkan Daftar Perangkat",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (showDeviceList) "🔼" else "🔽",
                        fontSize = 18.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = showDeviceList,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DeviceList(viewModel = viewModel)
            }

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "✨ Tambah Perangkat Baru",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Form fields
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Perangkat") },
                        placeholder = { Text("Contoh: Lampu Kamar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = daya,
                        onValueChange = { newValue ->
                            // Only allow digits, a single decimal point, and prevent multiple decimal points
                            val filteredValue = newValue.replace(Regex("[^0-9.]"), "")
                                .replace(Regex("\\.(?=.*\\.)"), "")

                            // Validate that it can be parsed as a Double if not empty
                            if (filteredValue.isEmpty() || filteredValue.toDoubleOrNull() != null) {
                                daya = filteredValue
                            }
                        },
                        label = { Text("Daya (Watt)") },
                        placeholder = { Text("Contoh: 25") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Category section with improved styling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Kategori Perangkat",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Dropdown kategori with improved style
                        DropdownKategori(
                            selectedKategori = kategori,
                            onKategoriSelected = { kategori = it }
                        )
                    }

                    // Time section with improved styling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Jadwal Penggunaan",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Time picker buttons
                            TimePickerDialogButton(
                                label = "Waktu Nyala",
                                time = waktuNyala,
                                onTimeSelected = { waktuNyala = it },
                            )

                            TimePickerDialogButton(
                                label = "Waktu Mati",
                                time = waktuMati,
                                onTimeSelected = { waktuMati = it },
                            )
                        }

                        // Duration display
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏱️ Durasi: ${"%.2f".format(durasi)} jam",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Invalid form warning
            AnimatedVisibility(visible = !isFormValid && (nama.isNotBlank() || daya.isNotBlank())) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = when {
                            nama.isBlank() -> "Nama perangkat tidak boleh kosong"
                            daya.isBlank() || daya.toDoubleOrNull() == null || daya.toDoubleOrNull()!! <= 0 ->
                                "Daya harus berupa angka positif"
                            durasi <= 0f -> "Durasi penggunaan harus lebih dari 0 jam"
                            else -> "Mohon isi semua data dengan benar"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    val dayaInt = daya.toIntOrNull() ?: 0
                    if (nama.isNotBlank() && dayaInt > 0 && durasi > 0f) {
                        viewModel.insertPerangkat(nama, dayaInt, kategori, waktuNyala, waktuMati, durasi)
                        nama = ""
                        daya = ""
                        kategori = KategoriPerangkat.OPSIONAL
                        waktuNyala = LocalTime.of(6, 0)
                        waktuMati = LocalTime.of(18, 0)
                        onPerangkatDisimpan()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = isFormValid
            ) {
                Text(
                    "💾 Simpan Perangkat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Add spacing at the bottom
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}