package com.example.voltix.ui.screen

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import com.example.voltix.data.entity.SimulationDeviceEntity
import com.example.voltix.data.entity.SimulationEntity
import com.example.voltix.ui.viewmodel.SimulasiBebasViewModel
import com.example.voltix.ui.viewmodel.TimeRange
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Paragraph
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.Nullable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R

import com.example.voltix.data.entity.RuanganEntity
import com.example.voltix.data.entity.jenis
import com.example.voltix.ui.Screen
import com.example.voltix.ui.component.DropdownKategori
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.ui.component.TimePickerDialogButton
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.launch
import java.io.File

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
    val timeRange by viewModel.timeRange.collectAsState()
    var showRoomDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showSimulationDialog by remember { mutableStateOf(simulationId == null) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFileNameDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }
    var fileNameError by remember { mutableStateOf<String?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<SimulationDeviceEntity?>(null) }
    var editingSimulation by remember { mutableStateOf<SimulationEntity?>(null) }
    var deletingSimulation by remember { mutableStateOf<SimulationEntity?>(null) }
    var waktuNyala by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var waktuMati by remember { mutableStateOf(LocalTime.of(23, 59)) }
    val dayaListrik by viewModel.totalDaya.collectAsState()
    val konsumsiListrik by viewModel.totalKonsumsi.collectAsState()
    val biayaListrik by viewModel.biayaListrik.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                showFileNameDialog = true
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Izin penyimpanan ditolak. Buka pengaturan aplikasi untuk mengizinkan."
                )
            }
        }
    }

    // Settings launcher
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            coroutineScope.launch {
                showFileNameDialog = true
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Izin penyimpanan diperlukan untuk menyimpan PDF di Android 9 atau lebih lama."
                )
            }
        }
    }

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
            TopBarS(onRoomSelectClick = { showRoomDialog = true },onBackClick = { navController.popBackStack() }) // atau aksi kembali lainnya)

        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
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
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Perangkat")
                }
                FloatingActionButton(
                    onClick = {
                        if (devices.isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Tambahkan setidaknya 1 perangkat untuk mengunduh PDF")
                            }
                        } else {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                if (shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showPermissionRationale = true
                                } else {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            } else {
                                fileNameInput = "SimulationReport_${System.currentTimeMillis()}"
                                showFileNameDialog = true
                            }
                        }
                    },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fa_download),
                        contentDescription = "Unduh PDF",
                        modifier = Modifier.size(24.dp)
                    )
                }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Atur Rentang Waktu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TimeRange.values().forEach { range ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.setTimeRange(range) }
                                .padding(horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = timeRange == range,
                                onClick = { viewModel.setTimeRange(range) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF3F51B5),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Text(
                                text = when (range) {
                                    TimeRange.DAILY -> "Harian"
                                    TimeRange.MONTHLY -> "Bulanan"
                                    TimeRange.YEARLY -> "Tahunan"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                                color = if (timeRange == range) Color(0xFF1A237E) else Color.Gray
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Log.d("SimulasiBebasScreen", "Showing loading indicator")
                    LoadingAnimationSection(isLoading)
                }
            } else if (devices.isEmpty() && simulationId != null) {
                item {
                    Log.d("SimulasiBebasScreen", "Showing EmptyStateMessage")
                    EmptyStateMessage()
                }
            } else {
                // DeviceList
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
                                    text = "Total daya perangkat melebihi batas listrik Anda! Daya listrik saat ini $dayaListrik",
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
                            onSelect = { onDeviceSelect(device) },
                            onEdit = {
                                editingDevice = device
                                waktuNyala = device.waktuNyala
                                waktuMati = device.waktuMati
                                showAddEditDialog = true
                            },
                            onDelete = { viewModel.deleteDevice(device.deviceId) }
                        )
                    }
                }

                // Informasi Listrik Simulasi
                item {
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
                                text = "Informasi Listrik Simulasi",
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
                                    color = Color.Black
                                )
                                Text(
                                    text = String.format("%.2f W", dayaListrik),
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
                                    text = "Total kWh Listrik",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = String.format("%.2f kWh", konsumsiListrik),
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
                                    color = Color.Black
                                )
                                Text(
                                    text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(biayaListrik),
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

    if (showEditNameDialog) {
        Log.d("SimulasiBebasScreen", "Showing SimulationEditNameDialog")
        SimulationEditNameDialog(
            simulation = editingSimulation,
            onSave = { simulationId, newName ->
                Log.d("SimulasiBebasScreen", "Updating simulation $simulationId to name: $newName")
                viewModel.updateSimulationName(simulationId, newName)
                showEditNameDialog = false
            },
            onDismiss = {
                Log.d("SimulasiBebasScreen", "SimulationEditNameDialog dismissed")
                showEditNameDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        Log.d("SimulasiBebasScreen", "Showing SimulationDeleteConfirmationDialog")
        SimulationDeleteConfirmationDialog(
            simulation = deletingSimulation,
            onConfirm = { simulationId ->
                Log.d("SimulasiBebasScreen", "Confirmed deletion of simulation $simulationId")
                viewModel.deleteSimulation(simulationId)
                showDeleteDialog = false
                if (simulationId == simulationId) {
                    navController.navigate(Screen.SimulasiPage.route)
                }
            },
            onDismiss = {
                Log.d("SimulasiBebasScreen", "SimulationDeleteConfirmationDialog dismissed")
                showDeleteDialog = false
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
            onEditSimulation = { simulation ->
                editingSimulation = simulation
                showEditNameDialog = true
            },
            onDeleteSimulation = { simulation ->
                deletingSimulation = simulation
                showDeleteDialog = true
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

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Izin Penyimpanan Diperlukan") },
            text = { Text("Aplikasi memerlukan izin penyimpanan untuk menyimpan laporan PDF di perangkat Anda.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                ) {
                    Text("Izinkan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionRationale = false }
                ) {
                    Text("Tolak")
                }
            }
        )
    }

    if (showFileNameDialog) {
        AlertDialog(
            onDismissRequest = { showFileNameDialog = false },
            title = { Text("Masukkan Nama File PDF") },
            text = {
                Column {
                    TextField(
                        value = fileNameInput,
                        onValueChange = {
                            fileNameInput = it
                            fileNameError = validateFileName(it)
                        },
                        label = { Text("Nama File") },
                        isError = fileNameError != null,
                        supportingText = {
                            if (fileNameError != null) {
                                Text(fileNameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Ekstensi .pdf akan ditambahkan otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileNameError == null && fileNameInput.isNotBlank()) {
                            showFileNameDialog = false
                            coroutineScope.launch {
                                generateAndSavePdf(
                                    context,
                                    devices,
                                    simulationList.find { it.id == simulationId }?.name ?: "Simulasi Tanpa Nama",
                                    dayaListrik,
                                    biayaListrik,
                                    timeRange,
                                    snackbarHostState,
                                    "${fileNameInput.trim()}.pdf"
                                )
                            }
                        }
                    },
                    enabled = fileNameError == null && fileNameInput.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFileNameDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    val activity = (context as? ComponentActivity) ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

private fun validateFileName(name: String): String? {
    if (name.isBlank()) return "Nama file tidak boleh kosong"
    if (name.contains(Regex("[/\\\\:*?\"<>|]"))) return "Nama file mengandung karakter tidak valid"
    if (name.length > 100) return "Nama file terlalu panjang (maks 100 karakter)"
    return null
}

private suspend fun generateAndSavePdf(
    context: Context,
    devices: List<SimulationDeviceEntity>,
    simulationName: String,
    totalDaya: Double,
    biayaListrik: Double,
    timeRange: TimeRange,
    snackbarHostState: SnackbarHostState,
    fileName: String
) {
    try {
        val writer: PdfWriter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Gagal membuat file PDF")
            PdfWriter(contentResolver.openOutputStream(uri))
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            PdfWriter(file)
        }

        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        val boldFont = PdfFontFactory.createFont("Helvetica-Bold")
        val regularFont = PdfFontFactory.createFont("Helvetica")
        val blueColor = DeviceRgb(63, 81, 181) // #3F51B5

        // Header
        document.add(
            Paragraph("Laporan Simulasi Bebas")
                .setFont(boldFont)
                .setFontSize(20f)
                .setFontColor(blueColor)
                .setMarginBottom(10f)
        )
        document.add(
            Paragraph("Simulasi: $simulationName")
                .setFont(boldFont)
                .setFontSize(14f)
                .setMarginBottom(10f)
        )
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        document.add(
            Paragraph("Dibuat pada: ${dateFormat.format(Date())}")
                .setFont(regularFont)
                .setFontSize(12f)
                .setMarginBottom(20f)
        )

        // Devices Table
        document.add(
            Paragraph("Daftar Perangkat")
                .setFont(boldFont)
                .setFontSize(14f)
                .setFontColor(blueColor)
                .setMarginBottom(10f)
        )
        val deviceTable = Table(floatArrayOf(2f, 1f, 1f, 1.5f, 1.5f, 1f, 1.5f))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBorder(SolidBorder(1f))
        // Headers
        listOf(
            "Perangkat", "Daya (W)", "Jumlah", "Waktu Nyala", "Waktu Mati", "Durasi (jam)", "Daya Terpakai (kWh)"
        ).forEach { header ->
            deviceTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header)
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setFontColor(blueColor))
                    .setBackgroundColor(DeviceRgb(230, 230, 250)) // Light blue background
                    .setPadding(5f)
                    .setBorder(SolidBorder(1f))
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        // Data
        devices.forEach { device ->
            val durationHours = if (device.waktuMati.isAfter(device.waktuNyala)) {
                Duration.between(device.waktuNyala, device.waktuMati).toHours().toDouble()
            } else {
                Duration.between(device.waktuNyala, LocalTime.MAX).toHours().toDouble() +
                        Duration.between(LocalTime.MIN, device.waktuMati).toHours().toDouble()
            }
            val devicePowerUsage = (device.daya * device.jumlah * durationHours) / 1000.0
            listOf(
                device.nama,
                device.daya.toString(),
                device.jumlah.toString(),
                device.waktuNyala.toString(),
                device.waktuMati.toString(),
                "%.2f".format(durationHours),
                "%.2f".format(devicePowerUsage)
            ).forEach { value ->
                deviceTable.addCell(
                    Cell()
                        .add(Paragraph(value)
                            .setFont(regularFont)
                            .setFontSize(10f))
                        .setPadding(5f)
                        .setBorder(SolidBorder(1f))
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }
        }
        document.add(deviceTable.setMarginBottom(20f))

        // Summary Table
        document.add(
            Paragraph("Ringkasan")
                .setFont(boldFont)
                .setFontSize(14f)
                .setFontColor(blueColor)
                .setMarginBottom(10f)
        )
        val summaryTable = Table(floatArrayOf(2f, 3f))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBorder(SolidBorder(1f))
        // Headers
        listOf("Metrik", "Nilai").forEach { header ->
            summaryTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header)
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setFontColor(blueColor))
                    .setBackgroundColor(DeviceRgb(230, 230, 250))
                    .setPadding(5f)
                    .setBorder(SolidBorder(1f))
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }
        // Data
        listOf(
            Pair("Total Daya", "%.2f kWh/%s".format(
                totalDaya,
                when (timeRange) {
                    TimeRange.DAILY -> "hari"
                    TimeRange.MONTHLY -> "bulan"
                    TimeRange.YEARLY -> "tahun"
                }
            )),
            Pair("Total Biaya", NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(biayaListrik))
        ).forEach { (metric, value) ->
            summaryTable.addCell(
                Cell()
                    .add(Paragraph(metric)
                        .setFont(boldFont)
                        .setFontSize(10f))
                    .setPadding(5f)
                    .setBorder(SolidBorder(1f))
                    .setTextAlignment(TextAlignment.LEFT)
            )
            summaryTable.addCell(
                Cell()
                    .add(Paragraph(value)
                        .setFont(regularFont)
                        .setFontSize(10f))
                    .setPadding(5f)
                    .setBorder(SolidBorder(1f))
                    .setTextAlignment(TextAlignment.LEFT)
            )
        }
        document.add(summaryTable)

        document.close()
        snackbarHostState.showSnackbar("PDF disimpan di folder Downloads: $fileName")
    } catch (e: Exception) {
        snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.message}")
    }
}

@Composable
private fun TopBarS(
    onRoomSelectClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color(0xFF1A237E)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Simulasi Listrik",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    fontSize = 28.sp
                )
            )
        }

        Button(
            onClick = onRoomSelectClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3F51B5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Template", color = Color.White)
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
    viewModel: SimulasiBebasViewModel = hiltViewModel(),
    devices: List<SimulationDeviceEntity>,
    melebihiDaya: Boolean,
    onSelect: (SimulationDeviceEntity) -> Unit,
    onEdit: (SimulationDeviceEntity) -> Unit,
    onDelete: (SimulationDeviceEntity) -> Unit
) {
    val dayaListrik by viewModel.totalDaya.collectAsState()
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
                            text = "Total daya perangkat melebihi batas listrik Anda! Daya listrik saat ini $dayaListrik",
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
private fun SimulationEditNameDialog(
    simulation: SimulationEntity?,
    onSave: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(simulation?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Nama Simulasi",
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
                    if (name.isNotBlank() && simulation != null) {
                        onSave(simulation.id, name)
                    }
                },
                enabled = name.isNotBlank() && simulation != null
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
private fun SimulationDeleteConfirmationDialog(
    simulation: SimulationEntity?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hapus Simulasi",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF1A237E)
                )
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin menghapus simulasi '${simulation?.name}'? Tindakan ini tidak dapat dibatalkan.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF424242)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    simulation?.let { onConfirm(it.id) }
                },
                enabled = simulation != null
            ) {
                Text("Hapus", color = Color(0xFFEF5350))
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
    onEditSimulation: (SimulationEntity) -> Unit,
    onDeleteSimulation: (SimulationEntity) -> Unit,
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
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
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
                                    Text(
                                        text = simulation.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color(0xFF1A237E)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSimulationSelect(simulation) }
                                            .padding(end = 8.dp)
                                    )
                                    Row {
                                        IconButton(onClick = { onEditSimulation(simulation) }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Color(0xFF3F51B5)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteSimulation(simulation) }) {
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