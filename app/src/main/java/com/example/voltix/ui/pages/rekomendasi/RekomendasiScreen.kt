package com.example.voltix.ui.pages.rekomendasi

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekomendasiScreen(
    navController: NavController,
    ruanganViewModel: RuanganViewModel = hiltViewModel(),
    rekomViewModel: RekomendasiViewModel = hiltViewModel()
) {
    // --- STATE MANAGEMENT ---
    val isLoading by ruanganViewModel.isLoading.collectAsState()
    val ruanganList by ruanganViewModel.allRuangan.collectAsState()

    var selectedRuanganId by remember { mutableIntStateOf(-1) }
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- PEMICU PENGAMBILAN DATA ---
    LaunchedEffect(key1 = Unit) {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        ruanganViewModel.loadRuanganForUser(firebaseUid)
    }

    // --- Efek samping saat ruangan dipilih ---
    LaunchedEffect(selectedRuanganId) {
        if (selectedRuanganId != -1) {
            ruanganViewModel.loadDetail(selectedRuanganId)
            ruanganViewModel.loadLampuFor(selectedRuanganId)
        }
    }

    val detail by ruanganViewModel.ruanganDetail.collectAsState(initial = null)
    val lampuWithPerangkat by ruanganViewModel.lampuWithPerangkat.collectAsState(initial = emptyList())
    val loadingRekomendasi by rekomViewModel.loading.observeAsState(initial = false)
    val error by rekomViewModel.error.observeAsState(initial = null)
    val result by rekomViewModel.result.observeAsState(initial = null)

    var buttonClicked by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonClicked) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150), label = "Button Scale Animation"
    )

    val roomIconRes = remember(detail) {
        detail?.ruangan?.jenisRuangan?.let { jenisRuangan ->
            when (jenisRuangan) {
                JenisRuangan.KamarTidur -> R.drawable.ic_fa_bed
                JenisRuangan.RuangTamu -> R.drawable.ic_fa_chair
                JenisRuangan.Dapur -> R.drawable.ic_fa_kitchen
                JenisRuangan.KamarMandi -> R.drawable.ic_fa_bathtub
                JenisRuangan.Lainnya -> R.drawable.ic_fa_room
            }
        }
    }

    // --- UI STRUCTURE ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rekomendasi Lampu", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                ruanganList.isEmpty() -> {
                    EmptyState(navController)
                }
                else -> {
                    MainContent(
                        ruanganList = ruanganList,
                        selectedRuanganId = selectedRuanganId,
                        onRuanganSelected = { id, nama ->
                            selectedRuanganId = id
                            textFieldValue = nama
                            expanded = false
                            scope.launch { rekomViewModel.resetResult() }
                        },
                        textFieldValue = textFieldValue,
                        onTextFieldValueChange = { textFieldValue = it },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        detail = detail,
                        lampuWithPerangkat = lampuWithPerangkat,
                        buttonClicked = buttonClicked,
                        onButtonClicked = { buttonClicked = true },
                        buttonScale = buttonScale,
                        rekomViewModel = rekomViewModel,
                        result = result,
                        error = error,
                        roomIconRes = roomIconRes,
                        navController = navController,
                        loadingRekomendasi = loadingRekomendasi
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val emptyAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_data_animation))
        LottieAnimation(
            composition = emptyAnimation,
            modifier = Modifier.size(150.dp),
            iterations = LottieConstants.IterateForever
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum ada ruangan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tambah ruangan baru untuk memulai.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("daftar_ruangan") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Tambah Ruangan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
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
    lampuWithPerangkat: List<com.example.voltix.data.entity.LampWithPerangkat>,
    buttonClicked: Boolean,
    onButtonClicked: () -> Unit,
    buttonScale: Float,
    rekomViewModel: RekomendasiViewModel,
    result: LampRecommendationResult?,
    error: String?,
    roomIconRes: Int?,
    navController: NavController,
    loadingRekomendasi: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Pilih Ruangan", modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = if (selectedRuanganId != -1 && !expanded) textFieldValue else if (expanded) textFieldValue else "Pilih Ruangan",
                    onValueChange = onTextFieldValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Ruangan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                ) {
                    ruanganList.forEach { ruangan ->
                        DropdownMenuItem(
                            text = { Text(ruangan.namaRuangan) },
                            onClick = { onRuanganSelected(ruangan.id, ruangan.namaRuangan) }
                        )
                    }
                }
            }
        }

        if (selectedRuanganId != -1) {
            detail?.let { d ->
                SectionCard(
                    title = "Detail Ruangan",
                    modifier = Modifier.fillMaxWidth(),
                    iconRes = roomIconRes
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Nama", d.ruangan.namaRuangan)
                        DetailRow("Jenis", d.ruangan.jenisRuangan.label)
                        DetailRow("Luas", "${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²")
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
                        DetailRow("Jumlah", "$totalQty lampu")
                        DetailRow("Total Lumen", "$totalLumen lm")
                        DetailRow("Total Daya", "%.2f W".format(totalPower))
                        if (area > 0) {
                            DetailRow("Densitas", "%.2f W/m²".format(currentDensity))
                        }

                        // Menampilkan animasi loading di dalam tombol
                        AnimatedContent(
                            targetState = loadingRekomendasi,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "Loading Button Animation"
                        ) { isLoading ->
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                                    CustomLoadingAnimation()
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onButtonClicked()
                                        detail?.let { d ->
                                            val firstLamp = lampuWithPerangkat.first()
                                            val input = LampRecommendationInput(
                                                jenisRuangan = d.ruangan.jenisRuangan,
                                                panjang = d.ruangan.panjangRuangan,
                                                lebar = d.ruangan.lebarRuangan,
                                                lampOutputLm = firstLamp.lumenPerLamp,
                                                lampEfficacy = (firstLamp.lumenPerLamp / firstLamp.dayaPerLamp).toInt()
                                            )
                                            rekomViewModel.calculateAndSave(
                                                input,
                                                null,
                                                selectedRuanganId,
                                                firstLamp.lampu.id
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .scale(buttonScale),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = detail != null && area > 0 && !loadingRekomendasi
                                ) {
                                    Text("Hitung Rekomendasi", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            } else {
                SectionCard(title = "Informasi Lampu", modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Belum ada lampu di ruangan ini.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        Button(
                            onClick = { navController.navigate("daftar_ruangan") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tambah Lampu", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            result?.let { r ->
                SectionCard(title = "Hasil Rekomendasi", modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)) + expandVertically(animationSpec = tween(durationMillis = 500)),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Jumlah Lampu", "${r.numberOfLamps}")
                            DetailRow("Total Lumen", "${r.totalLumen} lm")
                            DetailRow("Total Daya", "%.2f W".format(r.totalPowerWatt))
                            DetailRow("Densitas Daya", "%.2f W/m²".format(r.densityPower))
                            DetailRow("Status SNI", if (r.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI", color = if (r.withinStandard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
                SectionCard(title = "Perbandingan", modifier = Modifier.fillMaxWidth()) {
                    ComparisonTable(
                        currentQty = totalQty,
                        currentTotalLumen = totalLumen,
                        currentTotalPower = totalPower,
                        currentDensity = currentDensity,
                        recommendedResult = r
                    )
                }
            }

            error?.let { errorMsg ->
                SectionCard(title = "Error", modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(errorMsg, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
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
                            enabled = detail != null && lampuWithPerangkat.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Coba Lagi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
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
    onClick: (() -> Unit)? = null,
    iconRes: Int? = null,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 120),
        label = "Card Scale Animation"
    )

    Card(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                scale = 0.96f
                                tryAwaitRelease()
                                scale = 1f
                                onClick()
                            }
                        )
                    }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .alpha(0.07f)
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = 600)),
                    exit = fadeOut()
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CustomLoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading Animation")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 600, easing = EaseInOut), repeatMode = RepeatMode.Reverse),
        label = "Pulse 1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 600, easing = EaseInOut, delayMillis = 200), repeatMode = RepeatMode.Reverse),
        label = "Pulse 2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 600, easing = EaseInOut, delayMillis = 400), repeatMode = RepeatMode.Reverse),
        label = "Pulse 3"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.size(60.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).scale(pulse1).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
            Box(modifier = Modifier.size(12.dp).scale(pulse2).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
            Box(modifier = Modifier.size(12.dp).scale(pulse3).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Menghitung...", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
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
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kategori", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Saat Ini", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Rekomendasi", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis, maxLines = 1)
        Text(current, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis, maxLines = 1)
        Text(recommended, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}