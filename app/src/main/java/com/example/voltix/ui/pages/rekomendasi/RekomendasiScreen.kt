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
import androidx.compose.ui.graphics.Color
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
    var selectedRuanganId by remember { mutableIntStateOf(-1) }
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    var buttonClicked by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonClicked) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "Button Scale Animation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Lampu", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CustomLoadingAnimation()
                }
            } else {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionCard(
                            title = "Pilih Ruangan",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it; if (it) textFieldValue = "" }
                            ) {
                                OutlinedTextField(
                                    value = textFieldValue,
                                    onValueChange = { textFieldValue = it },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    label = { Text("Pilih Ruangan") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    singleLine = true,
                                    isError = ruanganList.isEmpty(),
                                    readOnly = true
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
                                    ruanganList.filter { textFieldValue.isEmpty() || it.namaRuangan.contains(textFieldValue, ignoreCase = true) }
                                        .forEach { ruangan ->
                                            DropdownMenuItem(
                                                text = { Text(ruangan.namaRuangan, style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp)) },
                                                onClick = {
                                                    selectedRuanganId = ruangan.id
                                                    textFieldValue = ruangan.namaRuangan
                                                    expanded = false
                                                    scope.launch { rekomViewModel.resetResult() }
                                                },
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                }
                            }
                            if (ruanganList.isEmpty()) {
                                Text(
                                    "Belum ada ruangan. Tambahkan terlebih dahulu.",
                                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.2.sp),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        if (selectedRuanganId != -1 && ruanganList.isNotEmpty()) {
                            detail?.let { d ->
                                SectionCard(
                                    title = "Detail Ruangan",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Nama: ${d.ruangan.namaRuangan}", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        Text("Jenis: ${d.ruangan.jenisRuangan.label}", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        Text("Luas: ${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                    }
                                }
                            }

                            val totalQty = lampuWithPerangkat.sumOf { it.jumlah }
                            val totalLumen = lampuWithPerangkat.sumOf { it.lumenTotal }
                            val totalPower = lampuWithPerangkat.sumOf { it.jumlah * it.dayaPerLamp.toDouble() }
                            val area = detail?.ruangan?.let { it.panjangRuangan * it.lebarRuangan } ?: 0f
                            val currentDensity = if (area > 0 && totalPower > 0) totalPower / area else 0.0

                            if (lampuWithPerangkat.isNotEmpty()) {
                                SectionCard(
                                    title = "Informasi Lampu Saat Ini",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Jumlah: $totalQty lampu", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        Text("Total Lumen: $totalLumen lm", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        Text("Total Daya: ${"%.2f".format(totalPower)} W", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        if (area > 0) {
                                            Text("Densitas: ${"%.2f".format(currentDensity)} W/m²", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                        } else {
                                            Text("Densitas: Tidak dapat dihitung (dimensi belum diatur)", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp), color = MaterialTheme.colorScheme.error)
                                        }
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
                                            modifier = Modifier.fillMaxWidth().height(48.dp).scale(buttonScale),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            enabled = detail != null && area > 0 && lampuWithPerangkat.isNotEmpty()
                                        ) {
                                            Text("Hitung Rekomendasi", style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp), color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            } else {
                                SectionCard(
                                    title = "Informasi Lampu",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Belum ada lampu di ruangan ini.", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                        Button(
                                            onClick = { navController.navigate("daftar_ruangan") },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Tambah Lampu", style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp), color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }

                            result?.let { r ->
                                SectionCard(
                                    title = "Hasil Rekomendasi",
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)) + expandVertically(animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Jumlah Lampu: ${r.numberOfLamps}", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                            Text("Total Lumen: ${r.totalLumen} lm", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                            Text("Total Daya: ${"%.2f".format(r.totalPowerWatt)} W", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                            Text("Densitas Daya: ${"%.2f".format(r.densityPower)} W/m²", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp))
                                            Text(
                                                if (r.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI",
                                                style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp),
                                                color = if (r.withinStandard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    SectionCard(
                                        title = "Perbandingan",
                                        modifier = Modifier.fillMaxWidth()
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
                            }

                            error?.let { errorMsg ->
                                SectionCard(
                                    title = "Error",
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(errorMsg, style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
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
                                                            rekomViewModel.calculateAndSave(input, null, selectedRuanganId, it)
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = detail != null && lampuWithPerangkat.isNotEmpty()
                                        ) {
                                            Text("Coba Lagi", style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.3.sp), color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)),
                exit = fadeOut()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CustomLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading Animation")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse 1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = EaseInOut, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse 2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = EaseInOut, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse 3"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.size(60.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(pulse1)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(pulse2)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(pulse3)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Menghitung...", style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.2.sp), color = MaterialTheme.colorScheme.onSurface)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Kategori", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp))
                Text("Saat Ini", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp))
                Text("Rekomendasi", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ComparisonTableRow("Jumlah Lampu", currentQty.toString(), recommendedResult.numberOfLamps.toString())
            ComparisonTableRow("Total Lumen", "$currentTotalLumen lm", "${recommendedResult.totalLumen} lm")
            ComparisonTableRow("Total Daya", "%.2f W".format(currentTotalPower), "%.2f W".format(recommendedResult.totalPowerWatt))
            ComparisonTableRow("Densitas Daya", "%.2f W/m²".format(currentDensity), "%.2f W/m²".format(recommendedResult.densityPower))
            ComparisonTableRow("Status SNI", "-", if (recommendedResult.withinStandard) "✅ Sesuai" else "❌ Melebihi")
        }
    }
}

@Composable
fun ComparisonTableRow(label: String, current: String, recommended: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.2.sp), overflow = TextOverflow.Ellipsis, maxLines = 1)
        Text(current, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.2.sp), overflow = TextOverflow.Ellipsis, maxLines = 1)
        Text(recommended, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.2.sp), overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}