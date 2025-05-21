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
import com.example.voltix.data.entity.LampWithPerangkat
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import com.example.voltix.ui.viewmodel.RekomendasiViewModel
import kotlinx.coroutines.launch
import com.example.voltix.data.entity.JenisRuangan

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

    // Logika untuk menentukan ikon ruangan berdasarkan jenis ruangan yang dipilih
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rekomendasi Lampu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
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
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
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
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CustomLoadingAnimation()
                    }
                }
                ruanganList.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val emptyAnimation by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.no_data_animation)
                        )
                        LottieAnimation(
                            composition = emptyAnimation,
                            modifier = Modifier.size(150.dp),
                            iterations = LottieConstants.IterateForever
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada ruangan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tambah ruangan baru dengan tombol di bawah!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                                "Tambah Ruangan",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                else -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // SectionCard "Pilih Ruangan" - Tanpa ikon watermark
                            SectionCard(
                                title = "Pilih Ruangan",
                                modifier = Modifier.fillMaxWidth(),
                                // iconRes TIDAK DIBERIKAN DI SINI
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it; if (it) textFieldValue = "" }
                                ) {
                                    OutlinedTextField(
                                        value = textFieldValue,
                                        onValueChange = { textFieldValue = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        label = {
                                            Text(
                                                if (textFieldValue.isEmpty() && !expanded)
                                                    "Pilih Ruangan"
                                                else
                                                    "Cari Ruangan"
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                        },
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        singleLine = true,
                                        isError = ruanganList.isEmpty(),
                                        placeholder = {
                                            Text(
                                                "Ketik untuk mencari...",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
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
                                        val filteredRuangan = ruanganList.filter {
                                            textFieldValue.isEmpty() ||
                                                    it.namaRuangan.contains(textFieldValue, ignoreCase = true)
                                        }
                                        if (filteredRuangan.isEmpty() && textFieldValue.isNotEmpty()) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        "Tidak ada ruangan ditemukan",
                                                        style = MaterialTheme.typography.titleMedium.copy( // Font lebih besar
                                                            letterSpacing = 0.2.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                        )
                                                    )
                                                },
                                                onClick = {},
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                enabled = false
                                            )
                                        } else {
                                            filteredRuangan.forEach { ruangan ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            ruangan.namaRuangan,
                                                            style = MaterialTheme.typography.titleMedium.copy( // Font lebih besar
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
                                }
                            }

                            if (selectedRuanganId != -1 && ruanganList.isNotEmpty()) {
                                detail?.let { d ->
                                    // SectionCard "Detail Ruangan" - Diberi ikon watermark
                                    SectionCard(
                                        title = "Detail Ruangan",
                                        modifier = Modifier.fillMaxWidth(),
                                        iconRes = roomIconRes // Hanya di sini ikon diteruskan
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            DetailRow("Nama", d.ruangan.namaRuangan)
                                            DetailRow("Jenis", d.ruangan.jenisRuangan.label)
                                            DetailRow(
                                                "Luas",
                                                "${d.ruangan.panjangRuangan} x ${d.ruangan.lebarRuangan} m²"
                                            )
                                        }
                                    }
                                }

                                val totalQty = lampuWithPerangkat.sumOf { it.jumlah }
                                val totalLumen = lampuWithPerangkat.sumOf { it.lumenTotal }
                                val totalPower = lampuWithPerangkat.sumOf { it.jumlah * it.dayaPerLamp.toDouble() }
                                val area = detail?.ruangan?.let { it.panjangRuangan * it.lebarRuangan } ?: 0f
                                val currentDensity = if (area > 0 && totalPower > 0) totalPower / area else 0.0

                                if (lampuWithPerangkat.isNotEmpty()) {
                                    // SectionCard "Informasi Lampu Saat Ini" - Tanpa ikon watermark
                                    SectionCard(
                                        title = "Informasi Lampu Saat Ini",
                                        modifier = Modifier.fillMaxWidth(),
                                        // iconRes TIDAK DIBERIKAN DI SINI
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            DetailRow("Jumlah", "$totalQty lampu")
                                            DetailRow("Total Lumen", "$totalLumen lm")
                                            DetailRow("Total Daya", "%.2f W".format(totalPower))
                                            if (area > 0) {
                                                DetailRow("Densitas", "%.2f W/m²".format(currentDensity))
                                            } else {
                                                Text(
                                                    "Densitas: Tidak dapat dihitung (dimensi belum diatur)",
                                                    style = MaterialTheme.typography.titleMedium.copy( // Font lebih besar
                                                        letterSpacing = 0.2.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.error
                                                )
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
                                        }
                                    }
                                } else {
                                    // SectionCard "Informasi Lampu" - Tanpa ikon watermark
                                    SectionCard(
                                        title = "Informasi Lampu",
                                        modifier = Modifier.fillMaxWidth(),
                                        // iconRes TIDAK DIBERIKAN DI SINI
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                "Belum ada lampu di ruangan ini.",
                                                style = MaterialTheme.typography.titleMedium.copy( // Font lebih besar
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

                                result?.let { r ->
                                    SectionCard(
                                        title = "Hasil Rekomendasi",
                                        modifier = Modifier.fillMaxWidth()
                                        // iconRes tidak diberikan untuk ini
                                    ) {
                                        AnimatedVisibility(
                                            visible = true,
                                            enter = fadeIn(animationSpec = tween(
                                                durationMillis = 500,
                                                easing = FastOutSlowInEasing
                                            )) + expandVertically(animationSpec = tween(
                                                durationMillis = 500,
                                                easing = FastOutSlowInEasing
                                            )),
                                            exit = shrinkVertically() + fadeOut()
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                DetailRow("Jumlah Lampu", "${r.numberOfLamps}")
                                                DetailRow("Total Lumen", "${r.totalLumen} lm")
                                                DetailRow("Total Daya", "%.2f W".format(r.totalPowerWatt))
                                                DetailRow("Densitas Daya", "%.2f W/m²".format(r.densityPower))
                                                DetailRow(
                                                    "Status SNI",
                                                    if (r.withinStandard) "✅ Sesuai SNI" else "❌ Melebihi SNI",
                                                    color = if (r.withinStandard)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                    SectionCard(
                                        title = "Perbandingan",
                                        modifier = Modifier.fillMaxWidth()
                                        // iconRes tidak diberikan untuk ini
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

                                error?.let { errorMsg ->
                                    SectionCard(
                                        title = "Error",
                                        modifier = Modifier.fillMaxWidth(),
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                        // iconRes tidak diberikan untuk ini
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                errorMsg,
                                                style = MaterialTheme.typography.titleMedium.copy( // Font lebih besar
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
                                                enabled = detail != null && lampuWithPerangkat.isNotEmpty(),
                                                modifier = Modifier.fillMaxWidth()
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
    onClick: (() -> Unit)? = null,
    iconRes: Int? = null, // Parameter untuk ikon watermark
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
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
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
            // Background Icon Watermark - hanya tampil jika iconRes tidak null
            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp) // Penyesuaian posisi
                        .alpha(0.07f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(durationMillis = 600)
                    ),
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
            style = MaterialTheme.typography.titleMedium.copy( // Menggunakan titleMedium
                letterSpacing = 0.2.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy( // Menggunakan titleMedium
                letterSpacing = 0.2.sp
            ),
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.size(60.dp)
        ) {
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Menghitung...",
            style = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 0.2.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
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
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            ComparisonTableRow(
                "Jumlah Lampu",
                currentQty.toString(),
                recommendedResult.numberOfLamps.toString()
            )
            ComparisonTableRow(
                "Total Lumen",
                "$currentTotalLumen lm",
                "${recommendedResult.totalLumen} lm"
            )
            ComparisonTableRow(
                "Total Daya",
                "%.2f W".format(currentTotalPower),
                "%.2f W".format(recommendedResult.totalPowerWatt)
            )
            ComparisonTableRow(
                "Densitas Daya",
                "%.2f W/m²".format(currentDensity),
                "%.2f W/m²".format(recommendedResult.densityPower)
            )
            ComparisonTableRow(
                "Status SNI",
                "-",
                if (recommendedResult.withinStandard) "✅ Sesuai" else "❌ Melebihi"
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 0.2.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            current,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 0.2.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            recommended,
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