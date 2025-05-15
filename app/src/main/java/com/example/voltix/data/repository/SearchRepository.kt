package com.example.voltix.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.saveable
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.voltix.data.entity.ElectronicInformationModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.File
import javax.inject.Inject

class SearchRepository @Inject constructor(private val savedStateHandle: SavedStateHandle) {
    private val _imageBitmap = mutableStateOf<Bitmap?>(null)
    val imageBitmap: Bitmap? get() = _imageBitmap.value

    private val _searchResults = mutableStateOf<List<ElectronicInformationModel>>(emptyList())
    val searchResults: List<ElectronicInformationModel> get() = _searchResults.value

    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    // Add SavedStateHandle for persistence
    var selectedDeviceName by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    var selectedWattage by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var selectedRuanganId by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var selectedLumen by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var selectedLampType by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    fun saveSelectedDevice(
        deviceName: String,
        wattage: Int,
        ruanganId: Int,
        lumen: Int? = null,
        lampType: String? = null
    ) {
        selectedDeviceName = deviceName
        selectedWattage = wattage
        selectedRuanganId = ruanganId
        selectedLumen = lumen
        selectedLampType = lampType
        Log.d("SearchRepository", "Saved device: name=$deviceName, wattage=$wattage, lumen=$lumen, lampType=$lampType")
    }

    suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "temp_image.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        file
    }

    suspend fun uploadImageToCloudinary(imageFile: File): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cloudinary = Cloudinary("cloudinary://596144183678554:kFbNQ9kzJJDdtpn4tDp-bVmSlss@dkgrlebsh")
            val response = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap())
            response["url"] as? String
        } catch (e: Exception) {
            Log.e("SearchRepository", "Cloudinary upload failed: ${e.message}")
            null
        }
    }

    fun processImage(context: Context, bitmap: Bitmap, callback: (String, Map<String, String>?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    val label = labels[0].text.lowercase()
                    if (label.contains("lamp") || label.contains("bulb")) {
                        // Deteksi lampu, lakukan OCR untuk lumen, watt, jenis
                        extractLampInfo(context, bitmap) { lampInfo ->
                            callback(label, lampInfo)
                        }
                    } else {
                        // Non-lampu, lanjutkan seperti biasa
                        callback(label, null)
                    }
                } else {
                    Toast.makeText(context, "No labels detected.", Toast.LENGTH_SHORT).show()
                    callback("", null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
                callback("", null)
            }
    }

    private fun extractLampInfo(context: Context, bitmap: Bitmap, callback: (Map<String, String>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                val lampInfo = mutableMapOf<String, String>()

                // Ekstrak lumen (handle various formats)
                val lumenRegex = Regex("""(\d+)\s*(lm|lumen|lumens)\b""", RegexOption.IGNORE_CASE)
                lumenRegex.find(text)?.let { match ->
                    lampInfo["lumen"] = match.groupValues[1]
                }

                // Ekstrak watt (handle various formats)
                val wattRegex = Regex("""(\d+)\s*[-]?\s*(W|w|Watt|watt|Watts|watts)\b""", RegexOption.IGNORE_CASE)
                wattRegex.find(text)?.let { match ->
                    lampInfo["watt"] = match.groupValues[1]
                }

                // Ekstrak jenis lampu (handle variations like "LED bulb")
                val lampTypeRegex = Regex("""\b(LED|CFL|Incandescent|Halogen|Fluorescent)\s*(bulb|lamp)?\b""", RegexOption.IGNORE_CASE)
                lampTypeRegex.find(text)?.let { match ->
                    lampInfo["lampType"] = match.groupValues[1].uppercase()
                }

                Log.d("SearchRepository", "Extracted text: '$text'")
                Log.d("SearchRepository", "LampInfo: $lampInfo")
                if (lampInfo.isEmpty()) {
                    Toast.makeText(context, "No lamp info detected.", Toast.LENGTH_SHORT).show()
                }
                callback(lampInfo)
            }
            .addOnFailureListener {
                Log.e("SearchRepository", "Text recognition failed: ${it.message}")
                Toast.makeText(context, "Failed to extract lamp info.", Toast.LENGTH_SHORT).show()
                callback(emptyMap())
            }
    }

    fun fetchSearchResults(context: Context, query: String, lampInfo: Map<String, String>?, callback: (List<ElectronicInformationModel>) -> Unit) {
        if (lampInfo != null && lampInfo.isNotEmpty()) {
            // Lampu terdeteksi, buat model langsung dari lampInfo
            val model = ElectronicInformationModel(
                title = "Lampu",
                link = "",
                displayedLink = "",
                snippet = "Lampu dengan ${lampInfo["lumen"] ?: "Unknown"} lumen, ${lampInfo["watt"] ?: "Unknown"} watt",
                deviceType = lampInfo["lampType"] ?: "Lampu",
                wattage = lampInfo["watt"] ?: "Unknown Wattage",
                lumen = lampInfo["lumen"] ?: "Unknown",
                lampType = lampInfo["lampType"] ?: "Unknown",
                thumbnailUrl = query // Gunakan URL Cloudinary dari query (jika tersedia)
            )
            Log.d("SearchRepository", "Created lamp model: $model")
            callback(listOf(model))
            return
        }

        // Non-lampu, lanjutkan seperti sebelumnya
        val apiKey = "2944a1f9bf7f9905febdc54bb585e2f3f284d5ad1372d0f68d1f5f89d52f2f0c"
        val url = "https://serpapi.com/search?engine=google_lens&url=$query&hl=en&api_key=$apiKey"
        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val visualMatches = response.getJSONArray("visual_matches")
                    val results = mutableListOf<ElectronicInformationModel>()

                    for (i in 0 until visualMatches.length()) {
                        val item = visualMatches.getJSONObject(i)
                        val title = item.optString("title", "No Title")
                        val snippet = item.optString("snippet", "No description available")
                        val thumbnailUrl = item.optString("thumbnail", "") // Ambil thumbnail

                        val wattRegex = Regex("""\b(\d+)\s*[-]?\s*(W|w|Watt|watt|Watts|watts)\b""")
                        val wattMatch = wattRegex.find(title) ?: wattRegex.find(snippet)
                        val wattInfo = wattMatch?.groupValues?.get(1) ?: "Unknown Wattage"
                        val deviceType = extractItemType(title)

                        if (wattMatch != null) {
                            results.add(
                                ElectronicInformationModel(
                                    title = title,
                                    link = item.optString("link", ""),
                                    displayedLink = item.optString("displayed_link", ""),
                                    snippet = snippet,
                                    deviceType = deviceType,
                                    wattage = wattInfo,
                                    thumbnailUrl = if (thumbnailUrl.isNotEmpty()) thumbnailUrl else null
                                )
                            )
                        }
                    }
                    Log.d("SearchRepository", "Non-lamp results: $results")
                    callback(results)
                } catch (e: JSONException) {
                    Log.e("SearchRepository", "JSON parsing error: ${e.message}")
                    callback(emptyList())
                }
            },
            { error ->
                Log.e("SearchRepository", "Volley error: ${error.message}")
                Toast.makeText(context, "No Result found.", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            })

        queue.add(jsonObjectRequest)
    }

    private fun extractItemType(title: String): String {
        val lower = title.lowercase()
        return when {
            "lampu" in lower || "bulb" in lower -> "Lampu"
            "kipas" in lower || "fan" in lower -> "Kipas Angin"
            "pengering rambut" in lower || "hair dryer" in lower -> "Pengering Rambut"
            "setrika" in lower || "iron" in lower -> "Setrika"
            "mesin cuci" in lower -> "Mesin Cuci"
            "heater" in lower || "pemanas" in lower -> "Pemanas"
            "oven" in lower || "microwave" in lower -> "Oven"
            "kulkas" in lower || "fridge" in lower -> "Kulkas"
            "kompor" in lower || "stove" in lower -> "Kompor"
            "rice cooker" in lower || "magic com" in lower -> "Penanak Nasi"
            "tv" in lower || "monitor" in lower -> "TV"
            "blender" in lower || "mixer" in lower -> "Blender/Mixer"
            "vacuum" in lower -> "Penyedot Debu"
            else -> "Tidak Diketahui"
        }
    }
}