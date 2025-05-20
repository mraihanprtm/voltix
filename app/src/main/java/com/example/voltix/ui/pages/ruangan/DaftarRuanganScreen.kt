package com.example.voltix.ui.pages.ruangan

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R
import com.example.voltix.data.entity.JenisRuangan
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DaftarRuanganScreen(
    navController: NavHostController,
    viewModel: RuanganViewModel = hiltViewModel()
) {
    val daftarRuangan by viewModel.allRuangan.observeAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<RuanganEntity?>(null) }
    var showEditDialog by remember { mutableStateOf<RuanganEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        isLoading = false
    }

    val fabScale by animateFloatAsState(
        targetValue = if (isLoading) 1f else 1.1f,
        animationSpec = tween(durationMillis = 200)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .shadow(12.dp, CircleShape)
                    .size(72.dp)
                    .scale(fabScale)
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
                    contentDescription = "Tambah Ruangan",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tombol back di pojok kiri atas
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp)) // Jarak antara icon dan teks
                Text(
                    text = "Daftar Ruangan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val loadingAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
                    LottieAnimation(
                        composition = loadingAnimation,
                        modifier = Modifier.size(100.dp),
                        iterations = LottieConstants.IterateForever
                    )
                }
            } else if (daftarRuangan.isEmpty()) {
                EmptyStateView()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daftarRuangan.size) { index ->
                        RuanganCard(
                            ruangan = daftarRuangan[index],
                            onClick = {
                                navController.navigate("detail_ruangan/${daftarRuangan[index].id}")
                            },
                            onDelete = {
                                showDeleteDialog = daftarRuangan[index]
                            },
                            onEdit = {
                                showEditDialog = daftarRuangan[index]
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddRuanganDialog(
            onConfirm = { nama, panjang, lebar, jenis ->
                if (nama.isNotBlank() && panjang > 0 && lebar > 0) {
                    viewModel.insertRuangan(
                        RuanganEntity(
                            namaRuangan = nama,
                            panjangRuangan = panjang,
                            lebarRuangan = lebar,
                            jenisRuangan = jenis
                        )
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("Ruangan berhasil ditambahkan")
                    }
                    showAddDialog = false
                }
            },
            onDismiss = {
                showAddDialog = false
            }
        )
    }

    // Delete Dialog
    showDeleteDialog?.let { ruangan ->
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteRuangan(ruangan)
                scope.launch {
                    snackbarHostState.showSnackbar("Ruangan berhasil dihapus")
                }
                showDeleteDialog = null
            },
            onDismiss = {
                showDeleteDialog = null
            }
        )
    }

    // Edit Dialog
    showEditDialog?.let { ruangan ->
        EditRuanganDialog(
            ruangan = ruangan,
            onConfirm = { updatedRuangan ->
                viewModel.updateRuangan(updatedRuangan)
                scope.launch {
                    snackbarHostState.showSnackbar("Ruangan berhasil diperbarui")
                }
                showEditDialog = null
            },
            onDismiss = {
                showEditDialog = null
            }
        )
    }
}

@Composable
fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "Belum ada ruangan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tambah ruangan baru dengan tombol di bawah!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RuanganCard(
    ruangan: RuanganEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 120)
    )

    val roomIconRes = when (ruangan.jenisRuangan) {
        JenisRuangan.KamarTidur -> R.drawable.ic_fa_bed
        JenisRuangan.RuangTamu -> R.drawable.ic_fa_chair
        JenisRuangan.Dapur -> R.drawable.ic_fa_kitchen
        JenisRuangan.KamarMandi -> R.drawable.ic_fa_bathtub
        JenisRuangan.Lainnya -> R.drawable.ic_fa_room
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scale = 0.96f
                        tryAwaitRelease()
                        scale = 1f
                        onClick()
                    }
                )
            }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
        ) {
            // Background Icon Watermark
            Image(
                painter = painterResource(id = roomIconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f) // Covers ~2/3 to 3/4 of card width
                    .aspectRatio(1f)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .alpha(0.07f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(animationSpec = tween(200)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val namaPendek = if (ruangan.namaRuangan.length > 14) {
                        ruangan.namaRuangan.take(14) + "..."
                    } else ruangan.namaRuangan

                    Text(
                        text = namaPendek,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${ruangan.panjangRuangan}m x ${ruangan.lebarRuangan}m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Text(
                        text = ruangan.jenisRuangan.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuanganDialog(
    onConfirm: (String, Float, Float, JenisRuangan) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var panjang by remember { mutableStateOf("") }
    var lebar by remember { mutableStateOf("") }
    var selectedJenis by remember { mutableStateOf(JenisRuangan.Lainnya) }
    var isFormValid by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Validasi form
    LaunchedEffect(nama, panjang, lebar) {
        isFormValid = nama.isNotBlank() &&
                panjang.toFloatOrNull()?.let { it > 0 } ?: false &&
                lebar.toFloatOrNull()?.let { it > 0 } ?: false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Tambah Ruangan Baru",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Ruangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = nama.isBlank() && nama.isNotEmpty()
                )
                OutlinedTextField(
                    value = panjang,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            panjang = newValue
                        }
                    },
                    label = { Text("Panjang (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = panjang.isNotEmpty() && (panjang.toFloatOrNull() == null || panjang.toFloatOrNull()!! <= 0)
                )
                OutlinedTextField(
                    value = lebar,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            lebar = newValue
                        }
                    },
                    label = { Text("Lebar (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = lebar.isNotEmpty() && (lebar.toFloatOrNull() == null || lebar.toFloatOrNull()!! <= 0)
                )
                Text(
                    text = "Jenis Ruangan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedJenis.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Ruangan") },
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
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        JenisRuangan.values().forEach { jenis ->
                            DropdownMenuItem(
                                text = { Text(jenis.label) }, // ✅ gunakan label, bukan name
                                onClick = {
                                    selectedJenis = jenis
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        onConfirm(
                            nama,
                            panjang.toFloatOrNull() ?: 0f,
                            lebar.toFloatOrNull() ?: 0f,
                            selectedJenis
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFormValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            ) {
                Text(
                    text = "Tambah",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Hapus Ruangan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text("Apakah Anda yakin ingin menghapus ruangan ini?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuanganDialog(
    ruangan: RuanganEntity,
    onConfirm: (RuanganEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf(ruangan.namaRuangan) }
    var panjang by remember { mutableStateOf(ruangan.panjangRuangan.toString()) }
    var lebar by remember { mutableStateOf(ruangan.lebarRuangan.toString()) }
    var selectedJenis by remember { mutableStateOf(ruangan.jenisRuangan) }
    var isFormValid by remember { mutableStateOf(true) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Validasi form
    LaunchedEffect(nama, panjang, lebar) {
        isFormValid = nama.isNotBlank() &&
                panjang.toFloatOrNull()?.let { it > 0 } ?: false &&
                lebar.toFloatOrNull()?.let { it > 0 } ?: false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Edit Ruangan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Ruangan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = nama.isBlank()
                )
                OutlinedTextField(
                    value = panjang,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            panjang = newValue
                        }
                    },
                    label = { Text("Panjang (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = panjang.isNotEmpty() && (panjang.toFloatOrNull() == null || panjang.toFloatOrNull()!! <= 0)
                )
                OutlinedTextField(
                    value = lebar,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            lebar = newValue
                        }
                    },
                    label = { Text("Lebar (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    isError = lebar.isNotEmpty() && (lebar.toFloatOrNull() == null || lebar.toFloatOrNull()!! <= 0)
                )
                Text(
                    text = "Jenis Ruangan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedJenis.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Ruangan") },
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
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        JenisRuangan.values().forEach { jenis ->
                            DropdownMenuItem(
                                text = { Text(jenis.name) },
                                onClick = {
                                    selectedJenis = jenis
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isFormValid) {
                        onConfirm(
                            ruangan.copy(
                                namaRuangan = nama,
                                panjangRuangan = panjang.toFloatOrNull() ?: ruangan.panjangRuangan,
                                lebarRuangan = lebar.toFloatOrNull() ?: ruangan.lebarRuangan,
                                jenisRuangan = selectedJenis
                            )
                        )
                    }
                },
                enabled = isFormValid && (
                        nama != ruangan.namaRuangan ||
                                panjang.toFloatOrNull() != ruangan.panjangRuangan ||
                                lebar.toFloatOrNull() != ruangan.lebarRuangan ||
                                selectedJenis != ruangan.jenisRuangan
                        ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFormValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            ) {
                Text(
                    text = "Simpan",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    )
}