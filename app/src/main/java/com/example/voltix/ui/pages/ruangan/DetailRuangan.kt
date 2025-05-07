package com.example.voltix.ui.pages.ruangan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.voltix.R
import com.example.voltix.data.entity.jenis
import com.example.voltix.data.entity.jenisLampu
import com.example.voltix.ui.Screen
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.viewmodel.simulasi.PerangkatViewModel
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun DetailRuangan(
    navController: NavHostController,
    ruanganId: Int,
    viewModel: PerangkatViewModel = hiltViewModel(),
    ruanganViewModel: RuanganViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val perangkatList by viewModel.perangkatByRuangan.observeAsState(initial = emptyList())
    val melebihiDaya by viewModel.melebihiDaya.collectAsState()
    val showEditDialog by viewModel::showEditDialog
    var isLoading by remember { mutableStateOf(true) }
    val namaRuangan by ruanganViewModel.namaRuangan.collectAsState()
    val dayaListrik by viewModel.totalDaya.collectAsState()
    val biayaListrik by viewModel.totalBiaya.collectAsState()

    // Update ketika ruanganId berubah
    LaunchedEffect(ruanganId) {
        viewModel.loadPerangkatByRuangan(ruanganId)
        ruanganViewModel.loadNamaRuangan(ruanganId)
        kotlinx.coroutines.delay(1000)
        isLoading = false
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.InputPerangkat.createRoute(ruanganId = ruanganId)) },
                shape = CircleShape,
                modifier = Modifier
                    .shadow(12.dp, CircleShape)
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    ),
                containerColor = Color.Transparent
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tambah Perangkat",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // Header Ruangan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detail Ruangan ${namaRuangan ?: ruanganId}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Content
            if (isLoading) {
                LoadingStateView()
            } else if (perangkatList.isEmpty()) {
                com.example.voltix.ui.component.EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_fa_list),
                                contentDescription = "List Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daftar Perangkat",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                    }

                    items(perangkatList) { perangkat ->
                        PerangkatCard(viewModel, perangkat, ruanganId, navController)
                    }

                    if (melebihiDaya) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 32.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_fa_exclamation_triangle),
                                        contentDescription = "Warning",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Total daya perangkat melebihi batas listrik Anda!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_fa_list),
                                contentDescription = "List Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kalkulasi Listrik",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Informasi Listrik",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Daya Listrik",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$dayaListrik",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Biaya Listrik",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "$biayaListrik",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Show EditPerangkatDialog when showEditDialog is true
        if (showEditDialog) {
            EditPerangkatDialog(viewModel, ruanganId)
        }
    }
}

@Composable
fun PerangkatCard(
    viewModel: PerangkatViewModel,
    perangkat: com.example.voltix.data.entity.PerangkatEntity,
    ruanganId: Int,
    navController: NavHostController
) {
    var isExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(500)
        ) + fadeIn(tween(500)),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(500)
        ) + fadeOut(tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { isExpanded = !isExpanded }
                .animateContentSize(animationSpec = tween(300))
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_plug),
                            contentDescription = "Device Icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = perangkat.nama,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        painter = painterResource(
                            id = if (isExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float
                        ),
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    val durasiPemakaian = viewModel.getDurasiPenggunaan(perangkat.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            PerangkatDetailRow(
                                label = "Besaran Watt",
                                value = "${perangkat.daya} Watt",
                                iconResource = R.drawable.ic_fa_plug
                            )
                            PerangkatDetailRow(
                                label = "Durasi Pemakaian",
                                value = "$durasiPemakaian jam",
                                iconResource = R.drawable.ic_fa_clock
                            )
                            PerangkatDetailRow(
                                label = "Jumlah Perangkat",
                                value = "${perangkat.jumlah}",
                                iconResource = R.drawable.ic_fa_hashtag
                            )
                            PerangkatDetailRow(
                                label = "Total Konsumsi",
                                value = "${String.format("%.2f", (perangkat.daya * perangkat.jumlah * durasiPemakaian) / 1000.0)} kWh",
                                iconResource = R.drawable.ic_fa_bolt
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.perangkatDiedit = perangkat
                                        viewModel.showEditDialog = true
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_fa_edit),
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deletePerangkat(perangkat) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_fa_trash),
                                        contentDescription = "Hapus",
                                        modifier = Modifier.size(24.dp)
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

@Composable
fun PerangkatDetailRow(label: String, value: String, iconResource: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun LoadingStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Memuat perangkat...",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPerangkatDialog(viewModel: PerangkatViewModel, ruanganId: Int) {
    val perangkat = viewModel.perangkatDiedit
    var nama by remember { mutableStateOf(perangkat?.nama ?: "") }
    var daya by remember { mutableStateOf(perangkat?.daya?.toString() ?: "") }
    var jumlah by remember { mutableStateOf(perangkat?.jumlah?.toString() ?: "") }
    var selectedJenis by remember { mutableStateOf(perangkat?.jenis ?: jenis.Lainnya) }
    var selectedJenisLampu by remember { mutableStateOf(jenisLampu.LED) }
    var lumen by remember { mutableStateOf("") }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var isFormValid by remember { mutableStateOf(false) }
    var isLampuDropdownExpanded by remember { mutableStateOf(false) } // Perbaikan dropdown

    // Inisialisasi data dari database untuk waktu dan lampu
    LaunchedEffect(perangkat) {
        if (perangkat != null) {
            // Ambil waktu dari RuanganPerangkatCrossRef
            viewModel.getCrossRef(perangkat.id, ruanganId)?.let { crossRef ->
                waktuNyala = crossRef.waktuNyala
                waktuMati = crossRef.waktuMati
            }
            // Ambil data LampuEntity jika ada
            if (perangkat.jenis == jenis.Lampu) {
                viewModel.getLampuByPerangkatId(perangkat.id)?.let { lampu ->
                    selectedJenisLampu = lampu.jenis
                    lumen = lampu.lumen.toString()
                }
            }
        }
    }

    // Validasi form
    LaunchedEffect(nama, daya, jumlah, selectedJenis, lumen) {
        isFormValid = nama.isNotBlank() &&
                daya.toIntOrNull() != null &&
                daya.toIntOrNull()!! > 0 &&
                jumlah.toIntOrNull() != null &&
                jumlah.toIntOrNull()!! > 0 &&
                (selectedJenis != jenis.Lampu || (lumen.toIntOrNull() != null && lumen.toIntOrNull()!! > 0))
    }

    AlertDialog(
        onDismissRequest = { viewModel.showEditDialog = false },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_fa_edit),
                    contentDescription = "Edit",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Perangkat",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama") },
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
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = nama.isBlank()
                )
                OutlinedTextField(
                    value = daya,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                            daya = newValue
                        }
                    },
                    label = { Text("Daya (W)") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_plug),
                            contentDescription = "Daya",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = jumlah.isNotEmpty() && (jumlah.toIntOrNull() == null || jumlah.toIntOrNull()!! <= 0)
                )
                Text(
                    text = "Jenis Elektronik",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                DropdownKategori(
                    selectedJenis = selectedJenis,
                    onJenisSelected = { selectedJenis = it },
                )
                // Lampu Section (jika jenis adalah Lampu)
                AnimatedVisibility(
                    visible = selectedJenis == jenis.Lampu,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Detail Lampu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            // Dropdown untuk Jenis Lampu
                            ExposedDropdownMenuBox(
                                expanded = isLampuDropdownExpanded,
                                onExpandedChange = { isLampuDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedJenisLampu.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jenis Lampu") },
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_fa_bolt),
                                            contentDescription = "Jenis Lampu",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .menuAnchor(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = isLampuDropdownExpanded,
                                    onDismissRequest = { isLampuDropdownExpanded = false }
                                ) {
                                    jenisLampu.values().forEach { jenis ->
                                        DropdownMenuItem(
                                            text = { Text(jenis.name) },
                                            onClick = {
                                                selectedJenisLampu = jenis
                                                isLampuDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Input untuk Lumen
                            OutlinedTextField(
                                value = lumen,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                        lumen = newValue
                                    }
                                },
                                label = { Text("Lumen") },
                                leadingIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_fa_bolt),
                                        contentDescription = "Lumen",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                isError = lumen.isNotEmpty() && (lumen.toIntOrNull() == null || lumen.toIntOrNull()!! <= 0)
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Jadwal Penggunaan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_fa_clock),
                                contentDescription = "Waktu Nyala",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TimePickerDialogButton(
                                label = "Waktu Nyala",
                                time = waktuNyala,
                                onTimeSelected = { waktuNyala = it }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_fa_clock),
                                contentDescription = "Waktu Mati",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TimePickerDialogButton(
                                label = "Waktu Mati",
                                time = waktuMati,
                                onTimeSelected = { waktuMati = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (perangkat != null) {
                        viewModel.editPerangkat(
                            nama = nama,
                            daya = daya.toIntOrNull() ?: 0,
                            kategori = selectedJenis,
                            jumlah = jumlah.toIntOrNull() ?: 1,
                            waktuNyala = waktuNyala,
                            waktuMati = waktuMati,
                            jenisLampu = if (selectedJenis == jenis.Lampu) selectedJenisLampu else null,
                            lumen = if (selectedJenis == jenis.Lampu) lumen.toIntOrNull() else null
                        )
                        viewModel.showEditDialog = false
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Simpan",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.showEditDialog = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}