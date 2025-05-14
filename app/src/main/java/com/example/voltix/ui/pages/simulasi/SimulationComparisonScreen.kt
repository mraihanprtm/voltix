package com.example.voltix.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltix.R
import com.example.voltix.data.entity.SimulationWithDevices
import com.example.voltix.ui.component.LoadingAnimationSection
import com.example.voltix.ui.viewmodel.ComparisonResult
import com.example.voltix.ui.viewmodel.SimulationComparisonViewModel
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfName.Table
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SimulationComparisonScreen(
    viewModel: SimulationComparisonViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val simulations by viewModel.simulations.observeAsState(initial = emptyList())
    val comparisonResults by viewModel.comparisonResults.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val selectedSimulations = remember { mutableStateListOf<SimulationWithDevices>() }
    var showComparison by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showFileNameDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }
    var fileNameError by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(Unit) {
        viewModel.loadAllSimulations()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        if (selectedSimulations.size >= 2) {
                            viewModel.compareSimulations(selectedSimulations)
                            showComparison = true
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pilih setidaknya 2 simulasi untuk membandingkan")
                            }
                        }
                    },
                    containerColor = if (selectedSimulations.size >= 2) Color(0xFF3F51B5) else Color(0xFFB0BEC5),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Bandingkan")
                }
                FloatingActionButton(
                    onClick = {
                        if (selectedSimulations.isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pilih setidaknya 1 simulasi untuk mengunduh PDF")
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
                                fileNameInput = "SimulationComparison_${System.currentTimeMillis()}"
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                LoadingAnimationSection(isLoading)
            } else if (simulations.isEmpty()) {
                EmptyStateView(navController)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SimulationList(
                        simulations = simulations,
                        selectedSimulations = selectedSimulations,
                        onToggleSelection = { simulation ->
                            if (simulation in selectedSimulations) {
                                selectedSimulations.remove(simulation)
                            } else {
                                selectedSimulations.add(simulation)
                            }
                            showComparison = false
                        }
                    )
                    AnimatedVisibility(visible = showComparison && comparisonResults.isNotEmpty()) {
                        ComparisonTable(comparisonResults = comparisonResults)
                    }
                }
            }
        }
    }

    // Permission Rationale Dialog
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

    // File Name Input Dialog
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
                                    selectedSimulations,
                                    comparisonResults,
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

@Composable
fun EmptyStateView(
    navController: NavHostController
) {
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
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_data_animation))
            LottieAnimation(
                composition = composition,
                modifier = Modifier.size(200.dp),
                iterations = LottieConstants.IterateForever
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum ada simulasi",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tambahkan simulasi baru dengan tombol di bawah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("daftar_ruangan") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Tambah Ruangan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Perbandingan Simulasi",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                fontSize = 28.sp
            )
        )
    }
}

@Composable
private fun SimulationList(
    simulations: List<SimulationWithDevices>,
    selectedSimulations: List<SimulationWithDevices>,
    onToggleSelection: (SimulationWithDevices) -> Unit
) {
    Column {
        simulations.forEach { simulation ->
            SimulationCard(
                simulation = simulation,
                isSelected = simulation in selectedSimulations,
                onToggleSelection = { onToggleSelection(simulation) }
            )
        }
    }
}

@Composable
private fun SimulationCard(
    simulation: SimulationWithDevices,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) Color(0xFF3F51B5) else Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleSelection() }
            ) {
                Text(
                    text = simulation.simulation.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A237E)
                    )
                )
                Text(
                    text = "Perangkat: ${simulation.devices.size}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
                Text(
                    text = "Daya: ${"%.2f".format(simulation.calculatePowerUsage())} kWh",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
                Text(
                    text = "Biaya: Rp ${"%.2f".format(simulation.calculateCost())}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF424242)
                    )
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3F51B5)
                )
            )
        }
    }
}

@Composable
private fun ComparisonTable(comparisonResults: List<ComparisonResult>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Hasil Perbandingan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E),
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F51B5))
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TableCell(text = "Simulasi", weight = 0.3f, color = Color.White)
                    TableCell(text = "Daya (kWh)", weight = 0.2f, color = Color.White)
                    TableCell(text = "Biaya (Rp)", weight = 0.2f, color = Color.White)
                    TableCell(text = "Hemat Daya", weight = 0.2f, color = Color.White)
                    TableCell(text = "Hemat Biaya", weight = 0.2f, color = Color.White)
                }
                comparisonResults.forEachIndexed { index, result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) Color(0xFFF5F7FA) else Color(0xFFECEFF1))
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableCell(text = result.simulationName, weight = 0.3f)
                        TableCell(text = "%.2f".format(result.powerUsage), weight = 0.2f)
                        TableCell(text = "%.2f".format(result.cost), weight = 0.2f)
                        TableCell(
                            text = if (result.powerSavings == 0.0) "Terbaik" else "+%.2f".format(result.powerSavings),
                            weight = 0.2f,
                            color = if (result.powerSavings == 0.0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        )
                        TableCell(
                            text = if (result.costSavings == 0.0) "Terbaik" else "+%.2f".format(result.costSavings),
                            weight = 0.2f,
                            color = if (result.costSavings == 0.0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, weight: Float, color: Color = Color(0xFF424242)) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            color = color
        ),
        modifier = Modifier
            .padding(horizontal = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun SimulationWithDevices.calculatePowerUsage(): Double {
    return devices.sumOf { device ->
        val durationHours = if (device.waktuMati.isAfter(device.waktuNyala)) {
            Duration.between(device.waktuNyala, device.waktuMati).toHours().toDouble()
        } else {
            Duration.between(device.waktuNyala, LocalTime.MAX).toHours().toDouble() +
                    Duration.between(LocalTime.MIN, device.waktuMati).toHours().toDouble()
        }
        (device.daya * device.jumlah * durationHours) / 1000.0
    }
}

private fun SimulationWithDevices.calculateCost(): Double {
    val costPerKWh = 1444.70
    return calculatePowerUsage() * costPerKWh
}

private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    val activity = (context as? androidx.activity.ComponentActivity) ?: return false
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
    selectedSimulations: List<SimulationWithDevices>,
    comparisonResults: List<ComparisonResult>,
    snackbarHostState: SnackbarHostState,
    fileName: String
) {
    try {
        val writer: PdfWriter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
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
        val greenColor = DeviceRgb(76, 175, 80) // #4CAF50
        val redColor = DeviceRgb(239, 83, 80) // #EF5350
        val blackColor = DeviceRgb(0, 0, 0) // Black

        // Header
        document.add(
            Paragraph("Laporan Perbandingan Simulasi")
                .setFont(boldFont)
                .setFontSize(20f)
                .setFontColor(blueColor)
                .setMarginBottom(10f)
        )
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        document.add(
            Paragraph("Dibuat pada: ${dateFormat.format(Date())}")
                .setFont(regularFont)
                .setFontSize(12f)
                .setMarginBottom(20f)
        )

        // Selected Simulations Table
        document.add(
            Paragraph("Simulasi yang Dipilih")
                .setFont(boldFont)
                .setFontSize(14f)
                .setFontColor(blueColor)
                .setMarginBottom(10f)
        )
        val simulationTable = Table(floatArrayOf(3f, 2f, 2f, 2f))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBorder(SolidBorder(1f))
        // Headers
        listOf(
            "Nama Simulasi", "Jumlah Perangkat", "Daya (kWh)", "Biaya (Rp)"
        ).forEach { header ->
            simulationTable.addHeaderCell(
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
        selectedSimulations.forEach { simulation ->
            listOf(
                simulation.simulation.name,
                simulation.devices.size.toString(),
                "%.2f".format(simulation.calculatePowerUsage()),
                "%.2f".format(simulation.calculateCost())
            ).forEach { value ->
                simulationTable.addCell(
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
        document.add(simulationTable.setMarginBottom(20f))

        // Comparison Results Table
        if (comparisonResults.isNotEmpty()) {
            document.add(
                Paragraph("Hasil Perbandingan")
                    .setFont(boldFont)
                    .setFontSize(14f)
                    .setFontColor(blueColor)
                    .setMarginTop(20f)
                    .setMarginBottom(10f)
            )
            val comparisonTable = Table(floatArrayOf(2f, 1.5f, 1.5f, 2f, 2f))
                .setWidth(UnitValue.createPercentValue(100f))
                .setBorder(SolidBorder(1f))
            // Headers
            listOf(
                "Simulasi", "Daya (kWh)", "Biaya (Rp)", "Hemat Daya", "Hemat Biaya"
            ).forEach { header ->
                comparisonTable.addHeaderCell(
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
            comparisonResults.forEach { result ->
                listOf(
                    Pair(result.simulationName, regularFont to blackColor),
                    Pair("%.2f".format(result.powerUsage), regularFont to blackColor),
                    Pair("%.2f".format(result.cost), regularFont to blackColor),
                    Pair(
                        if (result.powerSavings == 0.0) "Terbaik" else "+%.2f kWh".format(result.powerSavings),
                        regularFont to if (result.powerSavings == 0.0) greenColor else redColor
                    ),
                    Pair(
                        if (result.costSavings == 0.0) "Terbaik" else "+%.2f".format(result.costSavings),
                        regularFont to if (result.costSavings == 0.0) greenColor else redColor
                    )
                ).forEach { (value, style) ->
                    val (font, color) = style
                    comparisonTable.addCell(
                        Cell()
                            .add(Paragraph(value)
                                .setFont(font)
                                .setFontSize(10f)
                                .setFontColor(color))
                            .setPadding(5f)
                            .setBorder(SolidBorder(1f))
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                }
            }
            document.add(comparisonTable)
        }

        document.close()
        snackbarHostState.showSnackbar("PDF disimpan di folder Downloads: $fileName")
    } catch (e: Exception) {
        snackbarHostState.showSnackbar("Gagal membuat PDF: ${e.message}")
    }
}