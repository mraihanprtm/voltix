package com.example.voltix.ui.theme

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.voltix.viewmodel.ImageRecognitionViewModel

@Composable
fun ImagePickerScreen(
    navController: NavHostController,
    viewModel: ImageRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher untuk galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
        }
    }

    // Launcher untuk kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            bitmap = it
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pratinjau gambar
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Gambar yang dipilih",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol buka Google Lens
            Button(
                onClick = { viewModel.openGoogleLens(context, it) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buka dengan Google Lens")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol pilih sumber gambar
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Pilih Galeri")
            }

            Button(onClick = { cameraLauncher.launch() }) {
                Text("Ambil Foto")
            }
        }
    }
}
