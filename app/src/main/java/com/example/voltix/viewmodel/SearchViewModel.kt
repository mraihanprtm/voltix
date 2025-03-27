package com.example.voltix.viewmodel

import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.voltix.data.model.DataModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import org.json.JSONException
import java.io.File
import android.content.Context


class SearchViewModel : ViewModel() {
    // Simpan gambar ke file lokal
    fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "temp_image.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file
    }

    // Upload gambar ke Cloudinary
    fun uploadImageToCloudinary(imageFile: File, callback: (String?) -> Unit) {
        val cloudinary =
            Cloudinary("cloudinary://596144183678554:kFbNQ9kzJJDdtpn4tDp-bVmSlss@dkgrlebsh")

        Thread {
            try {
                val response = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap())
                val imageUrl = response["url"] as String
                callback(imageUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    // Proses gambar dengan ML Kit
    fun processImage(context: android.content.Context, bitmap: Bitmap, callback: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) {
                    callback(labels[0].text)
                } else {
                    Toast.makeText(context, "No labels detected.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchSearchResults(
        context: android.content.Context,
        query: String,
        callback: (List<DataModel>) -> Unit
    ) {
        val apiKey = "2944a1f9bf7f9905febdc54bb585e2f3f284d5ad1372d0f68d1f5f89d52f2f0c"
        val url = "https://serpapi.com/search?engine=google_lens&url=$query&hl=en&api_key=$apiKey"

        val queue = newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val visualMatches = response.getJSONArray("visual_matches")
                    val results = mutableListOf<DataModel>()

                    for (i in 0 until visualMatches.length()) {
                        val item = visualMatches.getJSONObject(i)

                        val title = item.optString("title", "No Title")
                        val snippet = item.optString("snippet", "No description available")

                        // Gunakan regex untuk mencari daya listrik dalam snippet atau title
                        val wattRegex = Regex("""\b(\d+)\s?(-|\s)?\s?(W|w|Watt|watt)\b""")
                        val wattMatch = wattRegex.find(title) ?: wattRegex.find(snippet)
                        val wattInfo = wattMatch?.value ?: "Unknown Wattage"

                        // Hanya simpan data jika memiliki informasi watt
                        if (wattMatch != null) {
                            results.add(
                                DataModel(
                                    title = title,
                                    link = item.optString("link", ""),
                                    displayedLink = item.optString("displayed_link", ""),
                                    snippet = "Jenis: ${extractItemType(title)}\nDaya: $wattInfo"
                                )
                            )
                        }
                    }
                    callback(results)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(context, "No Result found.", Toast.LENGTH_SHORT).show()
            })
        queue.add(jsonObjectRequest)
    }

    private fun extractItemType(title: String): String {
        val lowerTitle = title.lowercase()

        return when {
            // Penerangan
            Regex("lampu|bulb|led|neon|fluorescent|lamp", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Lampu"

            // Kipas & Pendingin
            Regex("kipas|fan|cooler|blower|ventilator", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Kipas Angin"

            // Pengering rambut
            Regex("pengering rambut|hair dryer|blow dryer", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Pengering Rambut"

            // Setrika
            Regex("setrika|iron", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Setrika"

            // Mesin Cuci
            Regex("mesin cuci|washing machine|washer", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Mesin Cuci"

            // Pemanas
            Regex("pemanas|heater|water heater", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Pemanas"

            // Oven & Pemanggang
            Regex("oven|pemanggang|toaster|microwave", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Oven/Pemanggang"

            // Kulkas & Pendingin
            Regex("kulkas|lemari es|refrigerator|fridge", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Kulkas"

            // Kompor
            Regex("kompor|stove|cooktop|gas burner", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Kompor"

            // Rice Cooker & Magic Com
            Regex("penanak nasi|magic com|rice cooker", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Penanak Nasi"

            // TV & Layar
            Regex("tv|televisi|screen|monitor|display", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "TV/Layar"

            // Blender & Mixer
            Regex("blender|mixer|juicer", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Blender/Mixer"

            // Vacuum Cleaner
            Regex("vacuum|penyedot debu", RegexOption.IGNORE_CASE).containsMatchIn(lowerTitle) -> "Penyedot Debu"

            else -> "Tidak Diketahui"
        }
    }

}
