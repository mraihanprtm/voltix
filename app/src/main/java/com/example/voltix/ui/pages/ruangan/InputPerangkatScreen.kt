package com.example.voltix.ui.pages.ruangan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.room.TypeConverters
import com.example.voltix.R
import com.example.voltix.data.database.Converters
import com.example.voltix.data.entity.jenis
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.viewmodel.simulasi.PerangkatViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@TypeConverters(Converters::class)
@Composable
fun InputPerangkatScreen(
    navController: NavHostController,
    ruanganId: Int,
    initialDeviceName: String = "",
    initialWattage: String = "",
    viewModel: PerangkatViewModel = hiltViewModel(),
    onPerangkatDisimpan: () -> Unit = {}
) {
    var nama by remember { mutableStateOf(initialDeviceName) }
    var daya by remember { mutableStateOf(initialWattage) }
    var jumlah by remember { mutableStateOf("1") }
    var selectedJenis by remember { mutableStateOf(jenis.Lainnya) }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var isFormValid by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Form validation
    LaunchedEffect(nama, daya, jumlah) {
        isFormValid = nama.isNotBlank() &&
                daya.toIntOrNull() != null &&
                daya.toIntOrNull()!! > 0 &&
                jumlah.toIntOrNull() != null &&
                jumlah.toIntOrNull()!! > 0
    }

    // Save button pulse animation
    val buttonScale by animateFloatAsState(
        targetValue = if (isFormValid && !isSaving) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Success animation on save
    val saveScale by animateFloatAsState(
        targetValue = if (isSaving) 1.2f else 1f,
        animationSpec = tween(200),
        finishedListener = {
            if (isSaving) {
                isSaving = false
                onPerangkatDisimpan()
                navController.popBackStack()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(tween(300)) + fadeIn(tween(300)),
            exit = scaleOut(tween(300)) + fadeOut(tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_plus),
                            contentDescription = "Add Device",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Input Perangkat",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Masukkan detail perangkat elektronik",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Form Fields
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(tween(300, delayMillis = 100)),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Perangkat") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_tag),
                            contentDescription = "Nama",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    isError = nama.isBlank() && nama.isNotEmpty()
                )

                OutlinedTextField(
                    value = daya,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            daya = newValue
                        }
                    },
                    label = { Text("Daya (Watt)") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_plug),
                            contentDescription = "Daya",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    isError = daya.isNotEmpty() && (daya.toIntOrNull() == null || daya.toIntOrNull()!! <= 0)
                )

                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            jumlah = newValue
                        }
                    },
                    label = { Text("Jumlah") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_hashtag),
                            contentDescription = "Jumlah",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    isError = jumlah.isNotEmpty() && (jumlah.toIntOrNull() == null || jumlah.toIntOrNull()!! <= 0)
                )
            }
        }

        // Kategori Section
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300, delayMillis = 200)) + slideInVertically(tween(300, delayMillis = 200)),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_list),
                            contentDescription = "Kategori",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kategori Perangkat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownKategori(
                        selectedJenis = selectedJenis,
                        onJenisSelected = { selectedJenis = it },
                    )
                }
            }
        }

        // Jam Penggunaan Section
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300, delayMillis = 300)) + slideInVertically(tween(300, delayMillis = 300)),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_clock),
                            contentDescription = "Jadwal",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Jadwal Penggunaan (Jam)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TimePickerDialogButton(
                            label = "Waktu Nyala",
                            time = waktuNyala,
                            onTimeSelected = { waktuNyala = it },
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TimePickerDialogButton(
                            label = "Waktu Mati",
                            time = waktuMati,
                            onTimeSelected = { waktuMati = it },
                        )
                    }
                }
            }
        }

        // Save Button
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300, delayMillis = 400)) + slideInVertically(tween(300, delayMillis = 400)),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
        ) {
            Button(
                onClick = {
                    if (isFormValid) {
                        isSaving = true
                        viewModel.insertPerangkatToRuangan(
                            nama = nama,
                            jumlah = jumlah.toIntOrNull() ?: 1,
                            daya = daya.toIntOrNull() ?: 0,
                            kategori = selectedJenis,
                            waktuNyala = waktuNyala,
                            waktuMati = waktuMati,
                            ruanganId = ruanganId
                        )
                        // Reset form
                        nama = ""
                        daya = ""
                        jumlah = "1"
                        selectedJenis = jenis.Lainnya
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (isSaving) saveScale else buttonScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isFormValid)
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            else
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                        )
                    )
                    .padding(vertical = 12.dp),
                enabled = isFormValid && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_fa_save),
                        contentDescription = "Save",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaving) "Menyimpan..." else "Simpan Perangkat",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}