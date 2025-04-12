package com.example.voltix.ui.pages.googlelens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavHostController
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.ui.component.SearchResultItem
import com.example.voltix.viewmodel.googlelens.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val viewModel: SearchViewModel = hiltViewModel()
    val context = LocalContext.current

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var searchResults by remember { mutableStateOf<List<ElectronicInformationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showEmptyState by remember { mutableStateOf(true) }

    val takeImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val extras = data?.extras
                imageBitmap = extras?.get("data") as Bitmap?

                imageBitmap?.let { bitmap ->
                    isLoading = true
                    showEmptyState = false
                    viewModel.saveBitmapToFile(context, bitmap) { file ->
                        viewModel.uploadImage(file) { imageUrl ->
                            if (imageUrl != null) {
                                viewModel.fetchResults(context, imageUrl) { results ->
                                    searchResults = results
                                    isLoading = false
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to upload image. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isLoading = false
                            }
                        }
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Electronics Scanner", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
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
                visible = imageBitmap != null,
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
                    imageBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured Image",
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
                        "Don't know what this device is?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Take a photo and let us identify your electronics and their power usage.",
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
                                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                takeImageLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("📷 Take Photo", fontSize = 16.sp)
                        }
                        if (imageBitmap != null) {
                            Button(
                                onClick = {
                                    imageBitmap?.let { bitmap ->
                                        isLoading = true
                                        showEmptyState = false
                                        viewModel.processImage(context, bitmap) { query ->
                                            viewModel.fetchResults(context, query) { results ->
                                                searchResults = results
                                                isLoading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("🔍 Identify", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                }
            }

            // Results
            when {
                searchResults.isNotEmpty() -> {
                    Text(
                        "Results (${searchResults.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        searchResults.forEach { result ->
                            SearchResultItem(result) { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }

                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Analyzing your device...",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                showEmptyState -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(8.dp, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📷", fontSize = 40.sp)
                            }
                        }
                        Text(
                            "No results yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Take a photo of your electronic device to get started!",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No matches found. Try another photo.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
