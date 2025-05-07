package com.example.voltix.viewmodel.googlelens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.ElectronicInformationModel
import com.example.voltix.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {

        // Restore imageBitmap from saved file path
        savedStateHandle.get<String>("imageFilePath")?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    _uiState.update { it.copy(imageBitmap = bitmap) }
                    Log.d("SearchViewModel", "Restored imageBitmap from $path")
                } else {
                    Log.w("SearchViewModel", "Image file not found: $path")
                }
            } catch (e: Exception) {
                setError("Gagal memuat gambar tersimpan: ${e.message}")
            }
        }
        // Restore searchResults
        savedStateHandle.get<Array<ElectronicInformationModel>>("searchResults")?.let { results ->
            _uiState.update { it.copy(searchResults = results.toList()) }
            Log.d("SearchViewModel", "Restored searchResults: ${results.size} items")
        }
        // Log ViewModel initialization
        Log.d("SearchViewModel", "Initialized ViewModel: $this")
    }

    override fun onCleared() {
        Log.d("SearchViewModel", "ViewModel cleared: $this")
        super.onCleared()
    }

    fun updateSearchResults(results: List<ElectronicInformationModel>) {
        _uiState.update { currentState ->
            currentState.copy(searchResults = results)
        }
        savedStateHandle["searchResults"] = results.toTypedArray()
        Log.d("SearchViewModel", "Saved searchResults to SavedStateHandle: ${results.size} items")
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        _uiState.update { currentState ->
            currentState.copy(imageBitmap = bitmap)
        }
        Log.d("SearchViewModel", "Updated imageBitmap: ${bitmap != null}")
    }

    fun updateIsLoading(loading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isLoading = loading)
        }
        Log.d("SearchViewModel", "Updated isLoading: $loading")
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, callback: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = repository.saveBitmapToFile(context, bitmap)
                savedStateHandle["imageFilePath"] = file.absolutePath
                callback(file)
                Log.d("SearchViewModel", "Saved bitmap to file: ${file.absolutePath}, saved to SavedStateHandle")
            } catch (e: Exception) {
                setError("Gagal menyimpan gambar: ${e.message}")
                Log.e("SearchViewModel", "Failed to save bitmap: ${e.stackTraceToString()}")
            }
        }
    }

    fun uploadImage(imageFile: File, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadImageToCloudinary(imageFile)
                callback(url)
                Log.d("SearchViewModel", "Uploaded image, url: $url")
            } catch (e: Exception) {
                setError("Gagal mengunggah gambar: ${e.message}")
                Log.e("SearchViewModel", "Failed to upload image: ${e.stackTraceToString()}")
                callback(null)
            }
        }
    }

    fun processImage(context: Context, bitmap: Bitmap, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.processImage(context, bitmap, callback)
                Log.d("SearchViewModel", "Processed image")
            } catch (e: Exception) {
                setError("Gagal memproses gambar: ${e.message}")
                Log.e("SearchViewModel", "Failed to process image: ${e.stackTraceToString()}")
            }
        }
    }

    fun fetchResults(context: Context, query: String, callback: (List<ElectronicInformationModel>) -> Unit) {
        viewModelScope.launch {
            try {
                repository.fetchSearchResults(context, query, callback)
                Log.d("SearchViewModel", "Fetched results for query: $query")
            } catch (e: Exception) {
                setError("Gagal mengambil hasil: ${e.message}")
                Log.e("SearchViewModel", "Failed to fetch results: ${e.stackTraceToString()}")
                callback(emptyList())
            }
        }
    }

    fun resetState() {
        _uiState.update { SearchUiState() }
        savedStateHandle.remove<String>("imageFilePath")
        savedStateHandle.remove<Array<ElectronicInformationModel>>("searchResults")
        Log.d("SearchViewModel", "Reset state, cleared SavedStateHandle")
    }

    private fun setError(error: String) {
        _uiState.update { currentState ->
            currentState.copy(error = error)
        }
        Log.e("SearchViewModel", "Error set: $error")
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
        Log.d("SearchViewModel", "Cleared error")
    }
}

data class SearchUiState(
    val imageBitmap: Bitmap? = null,
    val searchResults: List<ElectronicInformationModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)