package com.example.voltix.ui.pages.googlelens
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.ui.component.SearchResultItem
import com.example.voltix.viewmodel.googlelens.SearchViewModel

@Composable
fun SearchScreen() {
    val searchViewModel: SearchViewModel = hiltViewModel() // ✅ Ambil ViewModel yang benar
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var searchResults by remember { mutableStateOf<List<ElectronicInformationModel>>(emptyList()) }

    val takeImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val extras = data?.extras
            imageBitmap = extras?.get("data") as Bitmap?

            imageBitmap?.let { bitmap ->
                // Pass the bitmap to the ViewModel to save it
                searchViewModel.saveBitmapToFile(context, bitmap) { file ->
                    // After saving, upload the image
                    searchViewModel.uploadImage(file) { imageUrl ->
                        if (imageUrl != null) {
                            // After uploading, fetch search results
                            searchViewModel.fetchResults(context, imageUrl) { results ->
                                searchResults = results
                            }
                        } else {
                            Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takeImageLauncher.launch(takePictureIntent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Snap", fontSize = 18.sp)
            }

            Button(
                onClick = {
                    imageBitmap?.let { bitmap ->
                        // Process the image and get search results
                        searchViewModel.processImage(context, bitmap) { query ->
                            searchViewModel.fetchResults(context, query) { results ->
                                searchResults = results
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Get Search Results", fontSize = 18.sp)
            }
        }

        LazyColumn {
            items(searchResults) { result ->
                SearchResultItem(result) { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        }
    }
}
