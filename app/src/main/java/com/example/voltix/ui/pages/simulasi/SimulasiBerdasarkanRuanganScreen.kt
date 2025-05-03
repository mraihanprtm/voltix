package com.example.voltix.ui.screen

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltix.data.entity.PerangkatEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.ui.theme.VoltixTheme

@Composable
fun SimulasiBerdasarkanRuanganScreen(
    onRoomSelect: (String, List<PerangkatEntity>) -> Unit
) {
    // State management
    var rooms by remember {
        mutableStateOf(
            listOf(
                "Ruang Tamu" to mutableListOf(
                    PerangkatEntity(id = 1, nama = "Lampu LED", jumlah = 2, jenis = jenis.Lainnya, daya = 60),
                    PerangkatEntity(id = 2, nama = "Televisi OLED", jumlah = 1, jenis = jenis.Lainnya, daya = 200)
                ),
                "Kamar Tidur" to mutableListOf(
                    PerangkatEntity(id = 3, nama = "Lampu LED", jumlah = 1, jenis = jenis.Lainnya, daya = 60),
                    PerangkatEntity(id = 4, nama = "AC Inverter", jumlah = 1, jenis = jenis.Lainnya, daya = 1500)
                ),
                "Dapur" to mutableListOf(
                    PerangkatEntity(id = 5, nama = "Kulkas", jumlah = 1, jenis = jenis.Lainnya, daya = 300),
                    PerangkatEntity(id = 6, nama = "Microwave", jumlah = 1, jenis = jenis.Lainnya, daya = 800)
                )
            )
        )
    }
    var selectedRoom by remember { mutableStateOf<String?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<PerangkatEntity?>(null) }
    var isVisible by remember { mutableStateOf(true) } // Set true for preview

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopBar(
                selectedRoom = selectedRoom,
                onBackClick = { selectedRoom = null }
            )
        },
        floatingActionButton = {
            if (selectedRoom != null) {
                FloatingActionButton(
                    onClick = {
                        editingDevice = null
                        showAddEditDialog = true
                    },
                    containerColor = Color(0xFF3F51B5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Perangkat")
                }
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedRoom == null) {
                    RoomList(
                        rooms = rooms,
                        onSelect = { roomName, devices ->
                            selectedRoom = roomName
                            onRoomSelect(roomName, devices)
                        }
                    )
                } else {
                    val selectedDevices = rooms.find { it.first == selectedRoom }?.second ?: mutableListOf()
                    DeviceList(
                        devices = selectedDevices,
                        onSelect = { onRoomSelect(selectedRoom!!, selectedDevices) },
                        onEdit = { device ->
                            editingDevice = device
                            showAddEditDialog = true
                        },
                        onDelete = { device ->
                            rooms = rooms.map { (name, devices) ->
                                if (name == selectedRoom) {
                                    name to devices.filter { it.id != device.id }.toMutableList()
                                } else {
                                    name to devices
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialog with animations
    AnimatedVisibility(
        visible = showAddEditDialog && selectedRoom != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        AddEditDeviceDialog(
            device = editingDevice,
            onSave = { nama, daya ->
                rooms = rooms.map { (name, devices) ->
                    if (name == selectedRoom) {
                        if (editingDevice == null) {
                            name to (devices + PerangkatEntity(
                                id = devices.size + 1,
                                nama = nama,
                                jumlah = 1,
                                jenis = jenis.Lainnya,
                                daya = daya
                            )).toMutableList()
                        } else {
                            name to devices.map {
                                if (it.id == editingDevice?.id) it.copy(nama = nama, daya = daya)
                                else it
                            }.toMutableList()
                        }
                    } else {
                        name to devices
                    }
                }
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }
}

@Composable
private fun TopBar(selectedRoom: String?, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedRoom == null) {
            Text(
                text = "Simulasi Berdasarkan Ruangan",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    fontSize = 28.sp
                )
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF3F51B5)
                    )
                }
                Text(
                    text = selectedRoom,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A237E)
                    )
                )
            }
        }
    }
}

@Composable
private fun RoomList(
    rooms: List<Pair<String, MutableList<PerangkatEntity>>>,
    onSelect: (String, List<PerangkatEntity>) -> Unit
) {
    LazyColumn {
        items(rooms) { (roomName, devices) ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                RoomCard(
                    roomName = roomName,
                    devices = devices,
                    onSelect = { onSelect(roomName, devices) }
                )
            }
        }
    }
}

@Composable
private fun RoomCard(roomName: String, devices: List<PerangkatEntity>, onSelect: () -> Unit) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = roomName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A237E)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            devices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = device.nama,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        )
                    )
                    Text(
                        text = "${device.daya} W",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF424242)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F51B5)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Pilih", color = Color.White)
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<PerangkatEntity>,
    onSelect: () -> Unit,
    onEdit: (PerangkatEntity) -> Unit,
    onDelete: (PerangkatEntity) -> Unit
) {
    if (devices.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Belum ada perangkat di ruangan ini.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF424242).copy(alpha = 0.7f)
                )
            )
        }
    } else {
        LazyColumn {
            items(devices) { device ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    DeviceCard(
                        perangkat = device,
                        onSelect = onSelect,
                        onEdit = { onEdit(device) },
                        onDelete = { onDelete(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    perangkat: PerangkatEntity,
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
                    text = perangkat.nama,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A237E)
                    )
                )
                Text(
                    text = "${perangkat.daya} W",
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
private fun AddEditDeviceDialog(
    device: PerangkatEntity?,
    onSave: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf(device?.nama ?: "") }
    var daya by remember { mutableStateOf(device?.daya?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (device == null) "Tambah Perangkat" else "Edit Perangkat",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A237E)
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Perangkat") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color(0xFF424242)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = daya,
                    onValueChange = { daya = it },
                    label = { Text("Daya (Watt)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color(0xFF424242)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nama.isNotBlank() && daya.toIntOrNull() != null) {
                        onSave(nama, daya.toInt())
                    }
                },
                enabled = nama.isNotBlank() && daya.toIntOrNull() != null
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

@Preview(showBackground = true)
@Composable
fun SimulasiBerdasarkanRuanganScreenPreview() {
    VoltixTheme {
        SimulasiBerdasarkanRuanganScreen(
            onRoomSelect = { _, _ -> }
        )
    }
}