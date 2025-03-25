package com.example.voltix.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream

class ImageRecognitionViewModel : ViewModel() {
    fun openGoogleLens(context: Context, bitmap: Bitmap) {
        val imageUri = createTempImageFile(context, bitmap)

        if (imageUri != null) {
            try {
                // Metode 1: Buka langsung di browser Google Lens
                val lensIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://lens.google.com/uploadbyurl?url=${Uri.encode(imageUri.toString())}")
                }
                context.startActivity(lensIntent)
            } catch (e: Exception) {
                // Metode 2: Share ke Google Lens
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        setPackage("com.google.android.googlequicksearchbox")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Buka dengan Google Lens"))
                } catch (e2: Exception) {
                    // Fallback: Tampilkan pesan error
                    Toast.makeText(context, "Gagal membuka Google Lens", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempImageFile(context: Context, bitmap: Bitmap): Uri? {
        return try {
            // Buat file di direktori eksternal
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(
                "device_image_${System.currentTimeMillis()}",
                ".jpg",
                storageDir
            )

            // Kompresi dan simpan bitmap
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()

            // Dapatkan URI menggunakan FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Metode alternatif pencarian gambar
    fun searchImageOnline(context: Context, bitmap: Bitmap) {
        val imageUri = createTempImageFile(context, bitmap)

        if (imageUri != null) {
            try {
                // Konstruksi URL pencarian gambar
                val searchUrl = "https://www.google.com/searchbyimage?image_url=${Uri.encode(imageUri.toString())}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mencari gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}