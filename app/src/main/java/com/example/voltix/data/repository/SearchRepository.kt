package com.example.voltix.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.voltix.data.model.DataModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.File
import javax.inject.Inject

class SearchRepository @Inject constructor() {

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
            e.printStackTrace()
            null
        }
    }

    fun processImage(context: Context, bitmap: Bitmap, callback: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                if (labels.isNotEmpty()) callback(labels[0].text)
                else Toast.makeText(context, "No labels detected.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to process image.", Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchSearchResults(context: Context, query: String, callback: (List<DataModel>) -> Unit) {
        val apiKey = "2944a1f9bf7f9905febdc54bb585e2f3f284d5ad1372d0f68d1f5f89d52f2f0c"
        val url = "https://serpapi.com/search?engine=google_lens&url=$query&hl=en&api_key=$apiKey"
        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val visualMatches = response.getJSONArray("visual_matches")
                    val results = mutableListOf<DataModel>()

                    for (i in 0 until visualMatches.length()) {
                        val item = visualMatches.getJSONObject(i)
                        val title = item.optString("title", "No Title")
                        val snippet = item.optString("snippet", "No description available")

                        val wattRegex = Regex("""\b(\d+)\s?(-|\s)?\s?(W|w|Watt|watt)\b""")
                        val wattMatch = wattRegex.find(title) ?: wattRegex.find(snippet)
                        val wattInfo = wattMatch?.value ?: "Unknown Wattage"

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
