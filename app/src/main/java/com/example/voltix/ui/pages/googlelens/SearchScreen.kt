package com.example.voltix.ui.pages.googlelens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.ui.Screen
import com.example.voltix.ui.component.SearchResultItem
import com.example.voltix.viewmodel.googlelens.SearchViewModel
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    // Collect UI State
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showEmptyState by remember { mutableStateOf(uiState.imageBitmap == null && uiState.searchResults.isEmpty()) }

    // Update showEmptyState based on uiState
    LaunchedEffect(uiState.imageBitmap, uiState.searchResults) {
        showEmptyState = uiState.imageBitmap == null && uiState.searchResults.isEmpty()
    }

    // Camera launcher for taking photos
    val takeImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                viewModel.updateIsLoading(true)
                showEmptyState = false
                viewModel.updateImageBitmap(it)
                viewModel.saveBitmapToFile(context, it) { file ->
                    Log.d("SearchScreen", "Bitmap saved, uploading file: ${file.absolutePath}")
                    viewModel.uploadImage(file) { imageUrl ->
                        if (imageUrl != null) {
                            viewModel.fetchResults(context, imageUrl) { results ->
                                viewModel.updateSearchResults(results)
                                viewModel.updateIsLoading(false)
                                Log.d("SearchScreen", "Search results updated: ${results.size} items")
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Gambar gagal diunggah. Silakan coba lagi")
                            }
                            viewModel.updateIsLoading(false)
                        }
                    }
                }
            }
        }
    }

    // Gallery launcher for picking images
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                viewModel.updateIsLoading(true)
                showEmptyState = false
                viewModel.updateImageBitmap(bitmap)
                viewModel.saveBitmapToFile(context, bitmap) { file ->
                    Log.d("SearchScreen", "Bitmap saved, uploading file: ${file.absolutePath}")
                    viewModel.uploadImage(file) { imageUrl ->
                        if (imageUrl != null) {
                            viewModel.fetchResults(context, imageUrl) { results ->
                                viewModel.updateSearchResults(results)
                                viewModel.updateIsLoading(false)
                                Log.d("SearchScreen", "Search results updated: ${results.size} items")
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Gambar gagal diunggah. Silakan coba lagi")
                            }
                            viewModel.updateIsLoading(false)
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gambar tidak ditemukan")
                }
                viewModel.updateIsLoading(false)
            }
        }
    }

    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takeImageLauncher.launch(intent)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Izin kamera diperlukan")
            }
        }
    }

    // Gallery permission launcher
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Izin galeri diperlukan")
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Pemindai Elektronik", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (uiState.imageBitmap != null || uiState.searchResults.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        viewModel.resetState()
                        showEmptyState = true
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Pencarian direset")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Cari Barang Baru")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image preview
            AnimatedVisibility(
                visible = uiState.imageBitmap != null,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    uiState.imageBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Tangkapan Gambar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Instruction card
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Ingin tahu perangkat apa ini?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Ambil foto atau pilih dari galeri untuk mengidentifikasi.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("📷 Ambil Foto", fontSize = 16.sp)
                        }
                        Button(
                            onClick = {
                                galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                "🖼️ Dari Galeri",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                    if (uiState.imageBitmap != null) {
                        Button(
                            onClick = {
                                uiState.imageBitmap?.let { bitmap ->
                                    viewModel.updateIsLoading(true)
                                    showEmptyState = false
                                    viewModel.processImage(context, bitmap) { query ->
                                        viewModel.fetchResults(context, query) { results ->
                                            viewModel.updateSearchResults(results)
                                            viewModel.updateIsLoading(false)
                                            Log.d("SearchScreen", "Search results updated via processImage: ${results.size} items")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "🔍 Identifikasi",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Results
            when {
                uiState.searchResults.isNotEmpty() -> {
                    Text(
                        "Hasil (${uiState.searchResults.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.searchResults.forEach { result ->
                            SearchResultItem(
                                data = result,
                                onItemClick = { deviceName, wattage ->
                                    // TODO: Replace ruanganId=0 with actual ruanganId from previous screen
                                    navController.navigate(
                                        Screen.InputPerangkat.createRoute(
                                            ruanganId = 0,
                                            deviceName = deviceName,
                                            wattage = wattage.toString()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                !showEmptyState && uiState.searchResults.isEmpty() && !uiState.isLoading -> {
                    Text(
                        "Tidak ada hasil ditemukan.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    // Show loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Show error messages if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
}