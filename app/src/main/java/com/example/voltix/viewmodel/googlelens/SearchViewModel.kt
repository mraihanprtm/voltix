package com.example.voltix.viewmodel.googlelens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
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

    private var _currentRuanganId = mutableStateOf(0)
    val currentRuanganId: Int get() = _currentRuanganId.value

    fun updateRuanganId(ruanganId: Int) {
        _currentRuanganId.value = ruanganId
    }

    fun updateSearchResults(results: List<ElectronicInformationModel>) {
        _uiState.update { currentState ->
            currentState.copy(searchResults = results)
        }
    }

    fun updateImageBitmap(bitmap: Bitmap?) {
        _uiState.update { currentState ->
            currentState.copy(imageBitmap = bitmap)
        }
    }

    fun updateIsLoading(loading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isLoading = loading)
        }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, callback: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val file = repository.saveBitmapToFile(context, bitmap)
                callback(file)
            } catch (e: Exception) {
                setError(e.message ?: "Error saving image")
            }
        }
    }

    fun uploadImage(imageFile: File, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadImageToCloudinary(imageFile)
                callback(url)
            } catch (e: Exception) {
                setError(e.message ?: "Error uploading image")
                callback(null)
            }
        }
    }

    fun processImage(context: Context, bitmap: Bitmap, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.processImage(context, bitmap, callback)
            } catch (e: Exception) {
                setError(e.message ?: "Error processing image")
            }
        }
    }

    fun fetchResults(context: Context, query: String, callback: (List<ElectronicInformationModel>) -> Unit) {
        viewModelScope.launch {
            try {
                repository.fetchSearchResults(context, query, callback)
            } catch (e: Exception) {
                setError(e.message ?: "Error fetching results")
                callback(emptyList())
            }
        }
    }

    private fun setError(error: String) {
        _uiState.update { currentState ->
            currentState.copy(error = error)
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }
}

data class SearchUiState(
    val imageBitmap: Bitmap? = null,
    val searchResults: List<ElectronicInformationModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)