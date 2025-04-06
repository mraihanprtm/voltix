package com.example.voltix.viewmodel.googlelens

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor (
    private val repository: SearchRepository
) : ViewModel() {

    // Save bitmap to file
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, callback: (File) -> Unit) {
        viewModelScope.launch {
            val file = repository.saveBitmapToFile(context, bitmap)
            callback(file)
        }
    }

    // Upload the image to Cloudinary
    fun uploadImage(imageFile: File, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val url = repository.uploadImageToCloudinary(imageFile)
            callback(url)
        }
    }

    // Process the image with ML Kit and get labels
    fun processImage(context: Context, bitmap: Bitmap, callback: (String) -> Unit) {
        repository.processImage(context, bitmap, callback)
    }

    // Fetch search results from API based on the query
    fun fetchResults(context: Context, url: String, callback: (List<ElectronicInformationModel>) -> Unit) {
        repository.fetchSearchResults(context, url, callback)
    }
}

