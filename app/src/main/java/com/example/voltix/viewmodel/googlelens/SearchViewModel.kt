package com.example.voltix.viewmodel.googlelens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Gunakan MutableState untuk state
    private val _searchResults = mutableStateOf<List<ElectronicInformationModel>>(
        savedStateHandle.get<List<ElectronicInformationModel>>("searchResults") ?: emptyList()
    )
    val searchResults: List<ElectronicInformationModel> get() = _searchResults.value

    var imageBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Satu fungsi update untuk searchResults
    fun updateSearchResults(results: List<ElectronicInformationModel>) {
        _searchResults.value = results
        savedStateHandle["searchResults"] = results
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        imageBitmap = bitmap
    }

    fun updateIsLoading(loading: Boolean) {
        isLoading = loading
    }

    // Fungsi-fungsi lainnya
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, callback: (File) -> Unit) {
        viewModelScope.launch {
            val file = repository.saveBitmapToFile(context, bitmap)
            callback(file)
        }
    }

    fun uploadImage(imageFile: File, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val url = repository.uploadImageToCloudinary(imageFile)
            callback(url)
        }
    }

    fun processImage(context: Context, bitmap: Bitmap, callback: (String) -> Unit) {
        repository.processImage(context, bitmap, callback)
    }

    fun fetchResults(context: Context, url: String, callback: (List<ElectronicInformationModel>) -> Unit) {
        repository.fetchSearchResults(context, url, callback)
    }
}

