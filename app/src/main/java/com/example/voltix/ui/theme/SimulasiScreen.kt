// SimulasiScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
import android.content.ContentValues
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Paint
import androidx.navigation.NavHostController
import java.io.File
import java.io.FileOutputStream

@Composable
fun SimulasiPage(
    navController: NavHostController,
    viewModel: SimulasiViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Simulasi Konsumsi Listrik Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Simulasi Konsumsi Listrik", fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TableComparison(viewModel)
                        }
                    }
                }

                // Device List Title
                item {
                    Text("Daftar Perangkat Elektronik:", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                }

                // Device List
                item {
                    DeviceList(viewModel)
                }

                // Add Device Button
                item {
                    Button(
                        onClick = { viewModel.showTambahDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tambah Perangkat Baru")
                    }
                }

                // Hasil Perbandingan
                item {
                    HasilPerbandingan(viewModel)
                }

                // Download PDF Button
                item {
                    Button(
                        onClick = { generatePdf(context, viewModel) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Laporan PDF")
                    }
                }
            }

            // Dialogs and Warnings
            if (viewModel.showEditDialog) {
                EditPerangkatDialog(viewModel)
            }

            if (viewModel.showTambahDialog) {
                TambahPerangkatDialog(viewModel)
            }

            if (viewModel.melebihiDaya) {
                PeringatanMelebihiDaya(viewModel)
            }
        }
    )
}


@Composable
fun PeringatanMelebihiDaya(viewModel: SimulasiViewModel) {
    if (viewModel.melebihiDaya) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Red)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "⚠️ Total daya melebihi batas maksimum!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    "Kurangi penggunaan perangkat agar tidak terjadi pemadaman listrik.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HasilPerbandingan(viewModel: SimulasiViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF508CD5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Rincian Simulasi",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Tabel perbandingan
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Keterangan", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Perubahan", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                Divider(color = Color.White, thickness = 1.dp)

                // Data tabel
                val rows = listOf(
                    "Total Daya (W)" to viewModel.selisihDaya,
                    "Total Konsumsi Harian (kWh)" to viewModel.selisihKonsumsi,
                    "Estimasi Biaya Harian (Rp)" to viewModel.selisihBiaya
                )

                rows.forEach { (label, value) ->
                    val formattedValue = when (value) {
                        is Int -> value.toString() // Daya
                        is Double -> "%.2f".format(value) // Konsumsi harian
                        else -> "Rp ${(value as Double).roundToInt()}" // Biaya
                    }

                    val textColor = when {
                        (value as Number).toDouble() < 0 -> Color.Green // Jika ada penghematan
                        else -> Color.White
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = Color.White, modifier = Modifier.weight(1f))
                        Text(formattedValue, color = textColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                }

                Divider(color = Color.White, thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Menampilkan pesan hemat jika ada pengurangan pada daya, konsumsi, atau biaya
            val hematDaya = viewModel.selisihDaya < 0
            val hematKonsumsi = viewModel.selisihKonsumsi < 0
            val hematBiaya = viewModel.selisihBiaya < 0

            if (hematDaya || hematKonsumsi || hematBiaya) {
                Text(
                    "ANDA MENGHEMAT:",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (hematDaya) Text("💡 Daya: ${-viewModel.selisihDaya} W", color = Color.Green)
                if (hematKonsumsi) Text("⚡ Konsumsi: %.2f kWh".format(-viewModel.selisihKonsumsi), color = Color.Green)
                if (hematBiaya) Text("💰 Biaya: Rp ${-viewModel.selisihBiaya.roundToInt()}", color = Color.Green)
            }
        }
    }
}



@Composable
fun DeviceList(viewModel: SimulasiViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Text("Nama", modifier = Modifier.weight(1f))
                Text("Daya (W)", modifier = Modifier.weight(1f))
                Text("Jam Pakai", modifier = Modifier.weight(1f))
                Text("", modifier = Modifier.weight(1f))
            }
            Divider(color = Color.Black, thickness = 1.dp)

            // List Perangkat with fixed height or max height
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn {
                    items(viewModel.perangkat) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.nama, modifier = Modifier.weight(1f))
                            Text("${item.daya} W", modifier = Modifier.weight(1f))
                            Text("${item.durasi} Jam", modifier = Modifier.weight(1f))

                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.perangkatDiedit = item
                                        viewModel.showEditDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painterResource(id = android.R.drawable.ic_menu_edit),
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.hapusPerangkat(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painterResource(id = android.R.drawable.ic_menu_delete),
                                        contentDescription = "Hapus",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun EditPerangkatDialog(viewModel: SimulasiViewModel) {
    val perangkat = viewModel.perangkatDiedit ?: return
    var nama by remember { mutableStateOf(perangkat.nama) }
    var daya by remember { mutableStateOf(perangkat.daya.toString()) }
    var durasi by remember { mutableStateOf(perangkat.durasi.toString()) }

    AlertDialog(
        onDismissRequest = { viewModel.showEditDialog = false },
        title = { Text("Edit Perangkat") },
        text = {
            Column {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                OutlinedTextField(value = daya, onValueChange = { daya = it }, label = { Text("Daya (Watt)") })
                OutlinedTextField(value = durasi, onValueChange = { durasi = it }, label = { Text("Durasi (Jam)") })
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.editPerangkat(nama, daya.toIntOrNull() ?: perangkat.daya, durasi.toFloatOrNull() ?: perangkat.durasi) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.showEditDialog = false }) {
                Text("Batal")
            }
        }
    )
}


@Composable
fun TableComparison(viewModel: SimulasiViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Keterangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Sebelum", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Sesudah", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        Divider(color = Color.Black, thickness = 1.dp)

        // Data tabel
        val rows = listOf(
            "Total Daya (W)" to (viewModel.totalDayaSebelum to viewModel.totalDaya),
            "Total Konsumsi Harian (kWh)" to ("%.2f".format(viewModel.totalKonsumsiSebelum) to "%.2f".format(viewModel.totalKonsumsi)),
            "Estimasi Biaya Harian (Rp)" to ("Rp ${viewModel.totalBiayaSebelum.roundToInt()}" to "Rp ${viewModel.totalBiaya.roundToInt()}")
        )

        rows.forEach { (label, values) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(values.first.toString(), fontSize = 14.sp, modifier = Modifier.weight(1f))  // Sebelum
                Text(values.second.toString(), fontSize = 14.sp, modifier = Modifier.weight(1f)) // Sesudah
            }
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}


@Composable
fun TambahPerangkatDialog(viewModel: SimulasiViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.showTambahDialog = false },
        title = { Text("Tambah Perangkat Baru") },
        text = {
            Column {
                OutlinedTextField(value = viewModel.namaBaru, onValueChange = { viewModel.namaBaru = it }, label = { Text("Nama Perangkat") })
                OutlinedTextField(value = viewModel.dayaBaru, onValueChange = { viewModel.dayaBaru = it }, label = { Text("Daya (Watt)") })
                OutlinedTextField(value = viewModel.durasiBaru, onValueChange = { viewModel.durasiBaru = it }, label = { Text("Durasi (Jam)") })
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.tambahPerangkat() }) {
                Text("Tambah")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.showTambahDialog = false }) {
                Text("Batal")
            }
        }
    )
}

fun generatePdf(context: Context, viewModel: SimulasiViewModel) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply {
        color = Color.Black
    }

    val text = """ 
        Laporan Simulasi Listrik
        ---------------------------
        Total Daya: ${viewModel.totalDaya} W
        Konsumsi Harian: ${viewModel.totalKonsumsi} kWh
        Biaya Harian: Rp ${viewModel.totalBiaya}
    """.trimIndent()

    val textPaint = TextPaint().apply {
        textSize = 14f
    }

    val staticLayout = StaticLayout(
        text, textPaint, canvas.width - 40,
        Layout.Alignment.ALIGN_NORMAL, 1.5f, 1f, false
    )

    canvas.save()
    canvas.translate(20f, 50f)
    staticLayout.draw(canvas)
    canvas.restore()

    document.finishPage(page)
    val fileName = "Laporan_Simulasi.pdf"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        try {
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
                document.close()
                Toast.makeText(context, "Laporan berhasil diunduh ke folder Download", Toast.LENGTH_LONG).show()
            } ?: throw IOException("Gagal menyimpan file")
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan PDF", Toast.LENGTH_SHORT).show()
        }
    } else {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            Toast.makeText(context, "Laporan berhasil diunduh ke folder Download", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan PDF", Toast.LENGTH_SHORT).show()
        }
    }
}

data class Perangkat(val nama: String, val daya: Int, val durasi: Float)

val samplePerangkat = listOf(
    Perangkat("Kulkas", 150, 24f),
    Perangkat("AC", 900, 8f),
    Perangkat("Lampu", 20, 6f),
    Perangkat("TV", 100, 5f)
)