package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.voltix.R
import com.example.voltix.data.entity.JenisRuangan
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import com.example.voltix.domain.SmartRecommendationResult
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    val isLoading by ruanganViewModel.isLoading.collectAsState()
    val ruanganList by ruanganViewModel.allRuangan.collectAsState()

    var selectedRuanganId by remember { mutableIntStateOf(-1) }
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        ruanganViewModel.loadRuanganForUser(firebaseUid)
    }

    LaunchedEffect(selectedRuanganId) {
        if (selectedRuanganId != -1) {
            ruanganViewModel.loadDetail(selectedRuanganId)
            ruanganViewModel.loadLampuFor(selectedRuanganId)
            scope.launch { rekomViewModel.resetResult() }
        }
    }

    val detail by ruanganViewModel.ruanganDetail.collectAsState(initial = null)
    val lampuWithPerangkat by ruanganViewModel.lampuWithPerangkat.collectAsState(initial = emptyList())
    val loadingRekomendasi by rekomViewModel.loading.observeAsState(initial = false)
    val result by rekomViewModel.result.observeAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Lampu", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator()
                ruanganList.isEmpty() -> EmptyState(navController)
                else -> MainContent(
                    ruanganList = ruanganList,
                    selectedRuanganId = selectedRuanganId,
                    onRuanganSelected = { id, nama ->
                        selectedRuanganId = id; textFieldValue = nama; expanded = false
                    },
                    textFieldValue = textFieldValue,
                    onTextFieldValueChange = { textFieldValue = it },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    detail = detail,
                    lampuWithPerangkat = lampuWithPerangkat,
                    rekomViewModel = rekomViewModel,
                    result = result,
                    navController = navController,
                    loadingRekomendasi = loadingRekomendasi,
                    scope = scope
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    ruanganList: List<com.example.voltix.data.entity.RuanganEntity>,
    selectedRuanganId: Int,
    onRuanganSelected: (Int, String) -> Unit,
    textFieldValue: String,
    onTextFieldValueChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    detail: com.example.voltix.data.entity.RuanganWithPerangkat?,
    lampuWithPerangkat: List<LampWithPerangkat>,
    rekomViewModel: RekomendasiViewModel,
    result: SmartRecommendationResult?,
    navController: NavController,
    loadingRekomendasi: Boolean,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Pilih Ruangan", modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
                OutlinedTextField(
                    value = if (selectedRuanganId != -1 && !expanded) textFieldValue else if (expanded) textFieldValue else "Pilih Ruangan",
                    onValueChange = onTextFieldValueChange,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("Ruangan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    readOnly = true
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                    ruanganList.forEach { ruangan ->
                        DropdownMenuItem(
                            text = { Text(ruangan.namaRuangan) },
                            onClick = { onRuanganSelected(ruangan.id, ruangan.namaRuangan) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedRuanganId != -1, enter = fadeIn() + expandVertically()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                detail?.let { d ->
                    SectionCard(title = "Detail Ruangan", modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Jenis", d.ruangan.jenisRuangan.label)
                            DetailRow("Luas", "${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²")
                        }
                    }
                }

                if (lampuWithPerangkat.isNotEmpty()) {
                    val firstLamp = lampuWithPerangkat.first()
                    SectionCard(title = "Lampu Terpasang Saat Ini", modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow("Spesifikasi Lampu", "${firstLamp.dayaPerLamp}W / ${firstLamp.lumenPerLamp}lm per buah")
                            DetailRow("Jumlah", "${lampuWithPerangkat.sumOf { it.jumlah }} buah")

                            AnimatedContent(targetState = loadingRekomendasi, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "Loading Button Animation") { isLoading ->
                                if (isLoading) {
                                    Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) { CustomLoadingAnimation() }
                                } else {
                                    Button(
                                        onClick = {
                                            detail?.let { d ->
                                                val input = LampRecommendationInput(
                                                    jenisRuangan = d.ruangan.jenisRuangan,
                                                    panjang = d.ruangan.panjangRuangan,
                                                    lebar = d.ruangan.lebarRuangan,
                                                    lampOutputLm = firstLamp.lumenPerLamp,
                                                    lampPowerWatt = firstLamp.dayaPerLamp.toDouble()
                                                )
                                                rekomViewModel.calculateAndSave(input, null, selectedRuanganId, firstLamp.lampu.id)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = detail != null && (detail?.ruangan?.panjangRuangan ?: 0f) > 0f && !loadingRekomendasi
                                    ) {
                                        Text("Hitung Rekomendasi")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    SectionCard(title = "Informasi Lampu", modifier = Modifier.fillMaxWidth()) {
                        Text("Belum ada data lampu di ruangan ini. Silakan tambahkan perangkat lampu terlebih dahulu.", textAlign = TextAlign.Center)
                    }
                }
            }
        }

        result?.let { smartResult ->
            AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) + expandVertically(animationSpec = tween(400))) {
                val currentLamp = lampuWithPerangkat.firstOrNull()
                val currentSpec = currentLamp?.let { "${it.dayaPerLamp}W / ${it.lumenPerLamp}lm" } ?: "-"
                val currentLampCount = lampuWithPerangkat.sumOf { it.jumlah }
                val currentTotalLumen = lampuWithPerangkat.sumOf { it.lumenTotal }
                val currentTotalPower = lampuWithPerangkat.sumOf { it.jumlah * it.dayaPerLamp.toDouble() }
                val area = detail?.ruangan?.let { it.panjangRuangan * it.lebarRuangan } ?: 1f
                val currentDensity = if (area > 0) currentTotalPower / area else 0.0

                if (smartResult.isFeasible) {
                    SuccessResultView(
                        inputSpec = currentSpec,
                        currentLampCount = currentLampCount,
                        currentTotalLumen = currentTotalLumen,
                        currentTotalPower = currentTotalPower,
                        currentDensity = currentDensity,
                        recommendation = smartResult.calculation,
                        onRecalculate = { scope.launch { rekomViewModel.resetResult() } },
                        onFinish = { navController.popBackStack() }
                    )
                } else {
                    InefficientResultView(
                        inputSpec = currentSpec,
                        consequence = smartResult.calculation,
                        onRecalculate = { scope.launch { rekomViewModel.resetResult() } }
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessResultView(
    inputSpec: String,
    currentLampCount: Int,
    currentTotalLumen: Int,
    currentTotalPower: Double,
    currentDensity: Double,
    recommendation: LampRecommendationResult,
    onRecalculate: () -> Unit,
    onFinish: () -> Unit
) {
    SectionCard(title = "", modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.CheckCircle, "Success", tint = Color(0xFF16a34a), modifier = Modifier.size(28.dp))
                Text("Rekomendasi Optimal Ditemukan!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF16a34a))
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Anda membutuhkan:", style = MaterialTheme.typography.bodyMedium)
                    Text("${recommendation.numberOfLamps} Buah", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("Lampu (${inputSpec.split(" ")[0]} per buah)", style = MaterialTheme.typography.bodyMedium)
                    Text("Menghasilkan total ${recommendation.totalLumen} Lumen", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Perbandingan Hasil", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
                ComparisonTableRowHeader()
                HorizontalDivider()
                ComparisonTableRow("Spek per Lampu", inputSpec, "$inputSpec (Sama)")
                ComparisonTableRow("Jumlah Lampu", "$currentLampCount buah", "${recommendation.numberOfLamps} buah")
                ComparisonTableRow("Total Lumen", "$currentTotalLumen lm", "${recommendation.totalLumen} lm")
                ComparisonTableRow("Total Daya", "%.1f W".format(currentTotalPower), "%.1f W".format(recommendation.totalPowerWatt))

                val powerSaving = currentTotalPower - recommendation.totalPowerWatt
                if (powerSaving > 0 && currentTotalPower > 0) {
                    val percentage = (powerSaving / currentTotalPower) * 100
                    ComparisonTableRow("Penghematan Daya", "-", "✅ Hemat %.0f%%".format(percentage), recommendedColor = Color(0xFF16a34a))
                }

                ComparisonTableRow("Densitas Daya", "%.2f W/m²".format(currentDensity), "%.2f W/m² (OK!)".format(recommendation.densityPower))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRecalculate, modifier = Modifier.weight(1f)) { Text("Hitung Ulang") }
                Button(onClick = onFinish, modifier = Modifier.weight(1f)) { Text("Selesai") }
            }
        }
    }
}

@Composable
fun InefficientResultView(
    inputSpec: String,
    consequence: LampRecommendationResult,
    onRecalculate: () -> Unit
) {
    SectionCard(title = "", modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical=8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Warning, "Warning", tint = Color(0xFFd97706), modifier = Modifier.size(28.dp))
                Text("Lampu Kurang Efisien", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFd97706))
            }

            Text("Untuk mencapai standar terang, rekomendasi berikut akan MELEBIHI batas hemat energi SNI.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

            Column(Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text("Konsekuensi untuk Mencapai Terang Ideal:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom=8.dp))
                DetailRow("Spek per Lampu", inputSpec)
                DetailRow("Jumlah Lampu", "${consequence.numberOfLamps} buah")
                DetailRow("Total Daya", "%.1f W (Sangat Boros)".format(consequence.totalPowerWatt))
                DetailRow("Densitas Daya", "%.2f W/m² (❌ Melebihi Batas)".format(consequence.densityPower), color = MaterialTheme.colorScheme.error)
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    Icon(Icons.Filled.Search, "Saran", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Saran: Cari lampu LED dengan efikasi lebih tinggi (contoh: 10W, >1000 lumen).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Button(onClick = onRecalculate, modifier = Modifier.fillMaxWidth()) { Text("Coba dengan Lampu Lain") }
        }
    }
}

@Composable
fun ComparisonTableRowHeader() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Kategori", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.outline))
        Text("Saat Ini", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.outline))
        Text("Rekomendasi", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.outline))
    }
}

@Composable
fun ComparisonTableRow(label: String, current: String, recommended: String, recommendedColor: Color = LocalContentColor.current) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium)
        Text(current, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Text(recommended, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = recommendedColor)
    }
}

@Composable
private fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.shadow(2.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (title.isNotEmpty()) {
                Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                HorizontalDivider()
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text("$label:", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = color, textAlign = TextAlign.End)
    }
}

@Composable
private fun CustomLoadingAnimation() {
    CircularProgressIndicator()
}

// PERBAIKAN: Menambahkan kembali definisi Composable EmptyState yang hilang
@Composable
private fun EmptyState(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val emptyAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_data_animation))
        LottieAnimation(
            composition = emptyAnimation,
            modifier = Modifier.size(200.dp),
            iterations = LottieConstants.IterateForever
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Belum Ada Ruangan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Anda perlu menambahkan ruangan terlebih dahulu untuk mendapatkan rekomendasi lampu.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("daftar_ruangan") },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Tambah Ruangan Sekarang", style = MaterialTheme.typography.labelLarge)
        }
    }
}
