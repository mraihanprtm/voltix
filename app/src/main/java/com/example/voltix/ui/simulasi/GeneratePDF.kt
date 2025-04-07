package com.example.voltix.ui.simulasi


import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.ui.graphics.Paint
import com.example.voltix.viewmodel.googlelens.SimulasiViewModel.SimulasiViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

fun generatePdf(context: Context, viewModel: SimulasiViewModel, rentang: String, jumlahPeriode: Int) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(400, 700, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { color }

    val textPaint = TextPaint().apply { textSize = 14f }

    var yPosition = 50f

    fun drawText(text: String) {
        val staticLayout = StaticLayout(
            text, textPaint, canvas.width - 40,
            Layout.Alignment.ALIGN_NORMAL, 1.5f, 1f, false
        )
        canvas.save()
        canvas.translate(20f, yPosition)
        staticLayout.draw(canvas)
        canvas.restore()
        yPosition += staticLayout.height + 20
    }

    drawText("Laporan Simulasi Listrik\n---------------------------")

    // 1. Data Sebelum & Sesudah
    drawText("""
        Perbandingan Sebelum & Sesudah:
        - Total Daya: ${viewModel.totalDayaSebelum} W → ${viewModel.totalDaya} W
        - Konsumsi Harian: ${"%.2f".format(viewModel.totalKonsumsiSebelum)} kWh → ${"%.2f".format(viewModel.totalKonsumsi)} kWh
        - Estimasi Biaya Harian: Rp ${viewModel.totalBiayaSebelum.roundToInt()} → Rp ${viewModel.totalBiaya.roundToInt()}
    """.trimIndent())

    // 2. Selisih konsumsi listrik & biaya
    val selisihDaya = viewModel.totalDayaSebelum - viewModel.totalDaya
    val selisihKonsumsi = viewModel.totalKonsumsiSebelum - viewModel.totalKonsumsi
    val selisihBiaya = viewModel.totalBiayaSebelum - viewModel.totalBiaya

    drawText("""
        Penghematan Setelah Perubahan:
        - Selisih Daya: ${selisihDaya} W
        - Selisih Konsumsi: ${"%.2f".format(selisihKonsumsi)} kWh
        - Selisih Biaya: Rp ${selisihBiaya.roundToInt()}
    """.trimIndent())

    // 3. Perhitungan berdasarkan rentang waktu
    val faktor = when (rentang) {
        "Mingguan" -> jumlahPeriode * 7
        "Bulanan" -> jumlahPeriode * 30
        else -> jumlahPeriode
    }

    val totalDaya = viewModel.totalDaya * faktor
    val totalKonsumsi = viewModel.totalKonsumsi * faktor
    val totalBiaya = viewModel.totalBiaya * faktor

    drawText("""
        Hasil Simulasi Rentang ($rentang - $jumlahPeriode Periode):
        - Total Daya: ${totalDaya} W
        - Total Konsumsi: ${"%.2f".format(totalKonsumsi)} kWh
        - Estimasi Biaya: Rp ${totalBiaya.roundToInt()}
    """.trimIndent())

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