package com.example.voltix.ui.screen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.voltix.R
import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.ui.Screen
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.TimePickerDialogButton
import com.example.voltix.ui.viewmodel.SimulasiBebasViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SimulasiBebasScreen(
    onDeviceSelect: (SimulationDeviceEntity) -> Unit,
    navController: NavController,
    simulationId: Int? = null,
    viewModel: SimulasiBebasViewModel = hiltViewModel()
) {
    val devices by viewModel.devices.observeAsState(initial = emptyList())
    val ruanganList by viewModel.ruanganList.observeAsState(initial = emptyList())
    val simulationList by viewModel.simulationList.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val melebihiDaya by viewModel.melebihiDaya.collectAsState()
    var showRoomDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showSimulationDialog by remember { mutableStateOf(simulationId == null) }
    var editingDevice by remember { mutableStateOf<SimulationDeviceEntity?>(null) }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(23, 59)) }
    val dayaListrik by viewModel.totalDaya.collectAsState()
    val biayaListrik by viewModel.biayaListrik.collectAsState()

    LaunchedEffect(simulationId) {
        Log.d("SimulasiBebasScreen", "simulationId changed: $simulationId")
        if (simulationId != null) {
            viewModel.loadSimulation(simulationId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllSimulations()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopBar(onRoomSelectClick = { showRoomDialog = true })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("SimulasiBebasScreen", "FAB clicked, opening AddEditDeviceDialog")
                    editingDevice = null
                    waktuNyala = LocalTime.of(0, 0)
                    waktuMati = LocalTime.of(23, 59)
                    showAddEditDialog = true
                },
                containerColor = Color(0xFF3F51B5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Perangkat")
            }
        },
        bottomBar = {
            Button(
                onClick = { navController.navigate(Screen.SimulasiPage.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Selesai", color = Color.White, fontSize = 16.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            if (isLoading) {
                Log.d("SimulasiBebasScreen", "Showing loading indicator")
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (devices.isEmpty() && simulationId != null) {
                Log.d("SimulasiBebasScreen", "Showing EmptyStateMessage")
                EmptyStateMessage()
            } else {
                Log.d("SimulasiBebasScreen", "Showing DeviceList with ${devices.size} devices")
                DeviceList(
                    devices = devices,
                    melebihiDaya = melebihiDaya,
                    onSelect = onDeviceSelect,
                    onEdit = { device ->
                        Log.d("SimulasiBebasScreen", "Editing device: ${device.nama}")
                        editingDevice = device
                        waktuNyala = device.waktuNyala
                        waktuMati = device.waktuMati
                        showAddEditDialog = true
                    },
                    onDelete = { device ->
                        Log.d("SimulasiBebasScreen", "Deleting device: ${device.nama}")
                        viewModel.deleteDevice(device.deviceId)
                    }
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

    if (showRoomDialog) {
        Log.d("SimulasiBebasScreen", "Showing RoomSelectionDialog")
        RoomSelectionDialog(
            rooms = ruanganList,
            onRoomSelect = { ruangan ->
                Log.d("SimulasiBebasScreen", "Room selected: ${ruangan.namaRuangan}")
                viewModel.loadRoomDevices(ruangan.id)
                showRoomDialog = false
            },
            onDismiss = {
                Log.d("SimulasiBebasScreen", "RoomSelectionDialog dismissed")
                showRoomDialog = false
            }
        )
    }

    if (showAddEditDialog) {
        Log.d("SimulasiBebasScreen", "Showing AddEditDeviceDialog")
        AddEditDeviceDialog(
            device = editingDevice,
            waktuNyala = waktuNyala,
            waktuMati = waktuMati,
            onSave = { nama, daya, newWaktuNyala, newWaktuMati ->
                Log.d("SimulasiBebasScreen", "Saving device: $nama, $daya W")
                if (editingDevice == null) {
                    viewModel.insertDevice(nama, daya, newWaktuNyala, newWaktuMati)
                } else {
                    editingDevice?.let { device ->
                        viewModel.updateDevice(device, nama, daya, newWaktuNyala, newWaktuMati)
                    }
                }
                showAddEditDialog = false
            },
            onDismiss = {
                Log.d("SimulasiBebasScreen", "AddEditDeviceDialog dismissed")
                showAddEditDialog = false
            }
        )
    }

    if (showNameDialog) {
        Log.d("SimulasiBebasScreen", "Showing SimulationNameDialog")
        SimulationNameDialog(
            onSave = { name ->
                Log.d("SimulasiBebasScreen", "Saving simulation: $name")
                viewModel.startSimulation(name)
                showNameDialog = false
            },
            onDismiss = {
                Log.d("SimulasiBebasScreen", "SimulationNameDialog dismissed")
                showNameDialog = false
            }
        )
    }

    if (showSimulationDialog) {
        Log.d("SimulasiBebasScreen", "Showing SimulationSelectionDialog")
        SimulationSelectionDialog(
            simulations = simulationList,
            onSimulationSelect = { simulation ->
                viewModel.loadSimulation(simulation.id)
                showSimulationDialog = false
            },
            onNewSimulation = {
                showNameDialog = true
                showSimulationDialog = false
            },
            onDismiss = {
                showSimulationDialog = false
                navController.navigate(Screen.SimulasiPage.route)
            }
        )
    }
}

@Composable
private fun TopBar(onRoomSelectClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Simulasi Listrik",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                fontSize = 28.sp
            )
        )
        Button(
            onClick = onRoomSelectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3F51B5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Template Ruangan", color = Color.White)
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Belum ada perangkat.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
        )
        Text(
            text = "Tambah perangkat baru atau pilih ruangan untuk memulai simulasi.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF424242).copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<SimulationDeviceEntity>,
    melebihiDaya: Boolean,
    onSelect: (SimulationDeviceEntity) -> Unit,
    onEdit: (SimulationDeviceEntity) -> Unit,
    onDelete: (SimulationDeviceEntity) -> Unit
) {
    LazyColumn {
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
        items(devices) { device ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                DeviceCard(
                    device = device,
                    onSelect = { onSelect(device) },
                    onEdit = { onEdit(device) },
                    onDelete = { onDelete(device) }
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: SimulationDeviceEntity,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = device.nama,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A237E)
                    )
                )
                Text(
                    text = "${device.daya} W, ${device.waktuNyala} - ${device.waktuMati}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF3F51B5)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomSelectionDialog(
    rooms: List<RuanganEntity>,
    onRoomSelect: (RuanganEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Ruangan",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A237E)
                )
            )
        },
        text = {
            if (rooms.isEmpty()) {
                Text(
                    text = "Belum ada ruangan terdaftar.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
            } else {
                LazyColumn {
                    items(rooms) { ruangan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onRoomSelect(ruangan) }
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F7FA)
                            )
                        ) {
                            Text(
                                text = ruangan.namaRuangan,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF1A237E)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF3F51B5))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDeviceDialog(
    device: SimulationDeviceEntity?,
    waktuNyala: LocalTime,
    waktuMati: LocalTime,
    onSave: (String, Int, LocalTime, LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf(device?.nama ?: "") }
    var daya by remember { mutableStateOf(device?.daya?.toString() ?: "") }
    var jumlah by remember { mutableStateOf(device?.jumlah?.toString() ?: "1") }
    var selectedJenis by remember { mutableStateOf(jenis.Lainnya) }
    var waktuNyalaInput by remember { mutableStateOf(waktuNyala) }
    var waktuMatiInput by remember { mutableStateOf(waktuMati) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    contentDescription = if (device == null) "Tambah" else "Edit",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (device == null) "Tambah Perangkat" else "Edit Perangkat",
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
                    )
                )
                OutlinedTextField(
                    value = daya,
                    onValueChange = { daya = it },
                    label = { Text("Daya (W)") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_plug),
                            contentDescription = "Daya",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { jumlah = it },
                    label = { Text("Jumlah") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fa_hashtag),
                            contentDescription = "Jumlah",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "Jenis Elektronik",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                DropdownKategori(
                    selectedJenis = selectedJenis,
                    onJenisSelected = { selectedJenis = it }
                )
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
                                time = waktuNyalaInput,
                                onTimeSelected = { waktuNyalaInput = it }
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
                                time = waktuMatiInput,
                                onTimeSelected = { waktuMatiInput = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nama.isNotBlank() && daya.toIntOrNull() != null && jumlah.toIntOrNull() != null) {
                        onSave(nama, daya.toInt() * jumlah.toInt(), waktuNyalaInput, waktuMatiInput)
                    }
                },
                enabled = nama.isNotBlank() && daya.toIntOrNull() != null && jumlah.toIntOrNull() != null,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (nama.isNotBlank() && daya.toIntOrNull() != null && jumlah.toIntOrNull() != null)
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
                onClick = onDismiss,
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

@Composable
private fun SimulationNameDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nama Simulasi",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A237E)
                )
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Masukkan nama simulasi") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color(0xFF424242)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Simpan", color = Color(0xFF3F51B5))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF3F51B5))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SimulationSelectionDialog(
    simulations: List<SimulationEntity>,
    onSimulationSelect: (SimulationEntity) -> Unit,
    onNewSimulation: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pilih Simulasi",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A237E)
                )
            )
        },
        text = {
            Column {
                if (simulations.isEmpty()) {
                    Text(
                        text = "Belum ada simulasi tersimpan.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        )
                    )
                } else {
                    LazyColumn {
                        items(simulations) { simulation ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSimulationSelect(simulation) }
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F7FA)
                                )
                            ) {
                                Text(
                                    text = simulation.name,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF1A237E)
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNewSimulation,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buat Simulasi Baru", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color(0xFF3F51B5))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}