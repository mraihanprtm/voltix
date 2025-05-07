package com.example.voltix.ui.pages.ruangan

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.JenisRuangan
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.viewmodel.simulasi.RuanganViewModel
import kotlinx.coroutines.launch

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Daftar Ruangan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            }

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (daftarRuangan.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(daftarRuangan) { ruangan ->
                        RuanganCard(
                            ruangan = ruangan,
                            onClick = {
                                navController.navigate("detail_ruangan/${ruangan.id}")
                            },
                            onDelete = {
                                showDeleteDialog = ruangan
                            },
                            onEdit = {
                                showEditDialog = ruangan
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
fun RuanganCard(
    ruangan: RuanganEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .animateContentSize(animationSpec = tween(200))
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = ruangan.namaRuangan,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${ruangan.panjangRuangan}m x ${ruangan.lebarRuangan}m, ${ruangan.jenisRuangan}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Belum ada ruangan",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tambahkan ruangan baru dengan tombol di bawah",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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