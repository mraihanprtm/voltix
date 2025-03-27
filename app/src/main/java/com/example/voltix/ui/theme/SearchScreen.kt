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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.*
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.voltix.data.model.DataModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import org.json.JSONException
import java.io.File
import com.android.volley.Request
import com.example.voltix.ui.theme.SearchResultItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltix.viewmodel.SearchViewModel


@Composable
fun MainScreen() {
    val searchViewModel: SearchViewModel = viewModel() // ✅ Ambil ViewModel yang benar
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var searchResults by remember { mutableStateOf<List<DataModel>>(emptyList()) }

    val takeImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val extras = data?.extras
            imageBitmap = extras?.get("data") as Bitmap?

            imageBitmap?.let { bitmap ->
                val file = searchViewModel.saveBitmapToFile(context, bitmap) // ✅ Panggil dari ViewModel

                searchViewModel.uploadImageToCloudinary(file) { imageUrl -> // ✅ Panggil dari ViewModel
                    if (imageUrl != null) {
                        searchViewModel.fetchSearchResults(context, imageUrl) { results -> // ✅ Panggil dari ViewModel
                            searchResults = results
                        }
                    } else {
                        Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show()
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
                        searchViewModel.processImage(context, bitmap) { query -> // ✅ Panggil dari ViewModel
                            searchViewModel.fetchSearchResults(context, query) { results -> // ✅ Panggil dari ViewModel
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


