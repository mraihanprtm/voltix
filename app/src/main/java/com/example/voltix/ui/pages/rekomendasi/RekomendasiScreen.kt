package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    ruanganId: Int,
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    val ruanganList by ruanganViewModel.allRuangan.observeAsState(initial = emptyList())
    var selectedRuanganId by remember { mutableIntStateOf(-1) } // Default ke -1 (tidak ada ruangan dipilih)
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Muat data hanya jika selectedRuanganId valid
    LaunchedEffect(selectedRuanganId) {
        if (selectedRuanganId != -1) {
            ruanganViewModel.loadDetail(selectedRuanganId)
            ruanganViewModel.loadLampuFor(selectedRuanganId)
        }
    }

    val detail by ruanganViewModel.ruanganDetail.collectAsState(initial = null)
    val lampuWithPerangkat by ruanganViewModel.lampuWithPerangkat.collectAsState(initial = emptyList())
    val loading by rekomViewModel.loading.observeAsState(initial = false)
    val error by rekomViewModel.error.observeAsState(initial = null)
    val result by rekomViewModel.result.observeAsState(initial = null)

    // State untuk animasi tombol
    var buttonClicked by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonClicked) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "Button Scale Animation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rekomendasi Lampu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CustomLoadingAnimation()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Room Selection
                    Column {
                        Text(
                            "Pilih Ruangan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { newExpanded ->
                                expanded = newExpanded
                                if (newExpanded) {
                                    textFieldValue = "" // Reset textFieldValue when opening dropdown
                                }
                            }
                        ) {
                            OutlinedTextField(
                                value = textFieldValue,
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Pilih Ruangan") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                                singleLine = true,
                                isError = ruanganList.isEmpty(),
                                readOnly = true // Mengubah menjadi readOnly untuk mencegah input manual
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                ruanganList.filter {
                                    textFieldValue.isEmpty() || it.namaRuangan.contains(textFieldValue, ignoreCase = true)
                                }.forEach { ruangan ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                ruangan.namaRuangan,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    letterSpacing = 0.2.sp
                                                )
                                            )
                                        },
                                        onClick = {
                                            selectedRuanganId = ruangan.id
                                            textFieldValue = ruangan.namaRuangan
                                            expanded = false
                                            scope.launch { rekomViewModel.resetResult() }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        if (ruanganList.isEmpty()) {
                            Text(
                                "Belum ada ruangan. Tambahkan terlebih dahulu.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Tampilkan pesan jika belum ada ruangan dipilih
                    if (selectedRuanganId == -1 && ruanganList.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Silakan pilih ruangan untuk melihat detail dan rekomendasi.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Room Details
                    detail?.let { d ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Detail Ruangan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                                Text(
                                    "Nama: ${d.ruangan.namaRuangan}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Jenis: ${d.ruangan.jenisRuangan.label}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    )
                                )
                                Text(
                                    "Luas: ${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    )
                                )
                            }
                        }
                    }

                    // Lamp Information
                    if (lampuWithPerangkat.isNotEmpty() && selectedRuanganId != -1) {
                        // Aggregate lamp data
                        val totalQty = lampuWithPerangkat.sumOf { it.jumlah }
                        val totalLumen = lampuWithPerangkat.sumOf { it.lumenTotal }
                        val totalPower = lampuWithPerangkat.sumOf { it.jumlah * it.dayaPerLamp.toDouble() }
                        val area = detail?.ruangan?.let { it.panjangRuangan * it.lebarRuangan } ?: 0f
                        val currentDensity = if (area > 0 && totalPower > 0) totalPower / area else 0.0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Informasi Lampu Saat Ini",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                                Text(
                                    "Jumlah: $totalQty lampu",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    )
                                )
                                Text(
                                    "Total Lumen: $totalLumen lm",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    )
                                )
                                Text(
                                    "Total Daya: ${"%.2f".format(totalPower)} W",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    )
                                )
                                if (area > 0) {
                                    Text(
                                        "Densitas: ${"%.2f".format(currentDensity)} W/m²",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            letterSpacing = 0.2.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        "Densitas: Tidak dapat dihitung (dimensi ruangan belum diatur)",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            letterSpacing = 0.2.sp
                                        ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Detail Lampu",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                                LampuTable(lampuWithPerangkat = lampuWithPerangkat)

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        buttonClicked = true
                                        detail?.let { d ->
                                            val firstLamp = lampuWithPerangkat.first()
                                            val input = LampRecommendationInput(
                                                jenisRuangan = d.ruangan.jenisRuangan,
                                                panjang = d.ruangan.panjangRuangan,
                                                lebar = d.ruangan.lebarRuangan,
                                                lampOutputLm = firstLamp.lumenPerLamp,
                                                lampEfficacy = (firstLamp.lumenPerLamp / firstLamp.dayaPerLamp).toInt()
                                            )
                                            rekomViewModel.calculateAndSave(input, null, selectedRuanganId, firstLamp.lampu.id)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .scale(buttonScale),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = detail != null && area > 0 && lampuWithPerangkat.isNotEmpty()
                                ) {
                                    Text(
                                        "Hitung Rekomendasi",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 0.3.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                result?.let { r ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Perbandingan Saat Ini vs Rekomendasi",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.3.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + slideInVertically(),
                                        exit = fadeOut()
                                    ) {
                                        ComparisonTable(
                                            currentQty = totalQty,
                                            currentTotalLumen = totalLumen,
                                            currentTotalPower = totalPower,
                                            currentDensity = currentDensity,
                                            recommendedResult = r
                                        )
                                    }
                                }

                                if (area == 0f) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Dimensi ruangan (panjang/lebar) belum diatur. Silakan perbarui di pengaturan ruangan.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            letterSpacing = 0.2.sp
                                        ),
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else if (detail != null && selectedRuanganId != -1) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Belum ada lampu di ruangan ini.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { navController.navigate("daftar_ruangan") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        "Tambah Lampu",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 0.3.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Error State
                    error?.let { errorMsg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    errorMsg,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        letterSpacing = 0.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                TextButton(
                                    onClick = {
                                        detail?.let { d ->
                                            val input = lampuWithPerangkat.firstOrNull()?.let { item ->
                                                LampRecommendationInput(
                                                    jenisRuangan = d.ruangan.jenisRuangan,
                                                    panjang = d.ruangan.panjangRuangan,
                                                    lebar = d.ruangan.lebarRuangan,
                                                    lampOutputLm = item.lumenPerLamp,
                                                    lampEfficacy = (item.lumenPerLamp / item.dayaPerLamp).toInt()
                                                )
                                            }
                                            if (input != null) {
                                                lampuWithPerangkat.firstOrNull()?.lampu?.id?.let {
                                                    rekomViewModel.calculateAndSave(
                                                        input,
                                                        null,
                                                        selectedRuanganId,
                                                        it
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    enabled = detail != null && lampuWithPerangkat.isNotEmpty()
                                ) {
                                    Text(
                                        "Coba Lagi",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            letterSpacing = 0.3.sp
                                        ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Recommendation Result
                    result?.let { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Hasil Rekomendasi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.3.sp
                                    )
                                )
                                RecommendationResult(result = r)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading Animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation Animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { rotationZ = rotation }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Menghitung...",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LampuTable(lampuWithPerangkat: List<LampWithPerangkat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jenis",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Jumlah",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Total Lumen",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Lumen/Lampu",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Daya/Lampu",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Table Rows
            lampuWithPerangkat.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.lampu.jenis.toString(),
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = item.jumlah.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.lumenTotal} lm",
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.lumenPerLamp} lm",
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${item.dayaPerLamp.toInt()} W",
                        modifier = Modifier.weight(1.5f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                if (item != lampuWithPerangkat.last()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationResult(result: LampRecommendationResult) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Jumlah Lampu: ${result.numberOfLamps}",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            )
        )
        Text(
            "Total Lumen: ${result.totalLumen} lm",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            )
        )
        Text(
            "Total Daya: ${"%.2f".format(result.totalPowerWatt)} W",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            )
        )
        Text(
            "Densitas Daya: ${"%.2f".format(result.densityPower)} W/m²",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            )
        )
        Text(
            if (result.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp
            ),
            color = if (result.withinStandard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ComparisonTable(
    currentQty: Int,
    currentTotalLumen: Int,
    currentTotalPower: Double,
    currentDensity: Double,
    recommendedResult: LampRecommendationResult
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Kategori",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
                Text(
                    "Saat Ini",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
                Text(
                    "Rekomendasi",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Content rows
            ComparisonTableRow(
                label = "Jumlah Lampu",
                current = currentQty.toString(),
                recommended = recommendedResult.numberOfLamps.toString()
            )
            ComparisonTableRow(
                label = "Total Lumen",
                current = "$currentTotalLumen lm",
                recommended = "${recommendedResult.totalLumen} lm"
            )
            ComparisonTableRow(
                label = "Total Daya",
                current = "%.2f W".format(currentTotalPower),
                recommended = "%.2f W".format(recommendedResult.totalPowerWatt)
            )
            ComparisonTableRow(
                label = "Densitas Daya",
                current = "%.2f W/m²".format(currentDensity),
                recommended = "%.2f W/m²".format(recommendedResult.densityPower)
            )
            ComparisonTableRow(
                label = "Status SNI",
                current = "-",
                recommended = if (recommendedResult.withinStandard) "✅ Sesuai" else "❌ Melebihi"
            )
        }
    }
}

@Composable
fun ComparisonTableRow(
    label: String,
    current: String,
    recommended: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 0.2.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = current,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 0.2.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = recommended,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 0.2.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}