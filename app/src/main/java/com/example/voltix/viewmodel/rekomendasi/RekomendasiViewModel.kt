package com.example.voltix.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.voltix.data.entity.RekomendasiDetail
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity
import com.example.voltix.data.repository.RekomendasiRepository
import com.example.voltix.domain.LampRecommendationCalculator
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.LampRecommendationResult
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RekomendasiViewModel @Inject constructor(
    private val repository: RekomendasiRepository,
    private val calculator: LampRecommendationCalculator
) : ViewModel() {

    // LiveData untuk history rekomendasi per user
    private val _userHistory = MutableLiveData<List<RekomendasiDetail>>()
    val userHistory: LiveData<List<RekomendasiDetail>> = _userHistory

    // LiveData untuk rekomendasi publik (tanpa user)
    private val _publicRecommendations = MutableLiveData<List<RekomendasiDetail>>()
    val publicRecommendations: LiveData<List<RekomendasiDetail>> = _publicRecommendations

    private val _result = MutableLiveData<LampRecommendationResult>()
    val result: LiveData<LampRecommendationResult> = _result

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Load history rekomendasi berdasarkan userId
    fun loadUserHistory(userId: Int) {
        viewModelScope.launch {
            val list = repository.getUserHistory(userId)
            _userHistory.value = list
        }
    }

    // Load rekomendasi publik (userId IS NULL)
    fun loadPublicRecommendations() {
        viewModelScope.launch {
            val list = repository.getPublicRekomendasi()
            _publicRecommendations.value = list
        }
    }

    // Simpan rekomendasi baru
    fun saveRecommendation(rekom: RekomendasiPenghematanLampuEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertRekomendasi(rekom)
            onComplete?.invoke()
        }
    }

    // Ambil detail per id rekom
    private val _detail = MutableLiveData<RekomendasiDetail>()
    val detail: LiveData<RekomendasiDetail> = _detail

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            val d = repository.getRekomendasiDetail(id)
            _detail.value = d
        }
    }

    fun calculateAndSave(
        input: LampRecommendationInput,
        userId: Int? = null,
        ruanganId: Int,
        lampuId: Int
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val calcRes = calculator.calculate(input)
                val rekomEntity = RekomendasiPenghematanLampuEntity(
                    userId = userId,
                    ruanganId = ruanganId,
                    lampuId = lampuId,
                    tanggal = Date()
                )
                repository.insertRekomendasi(rekomEntity)
                _result.value = calcRes
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }
}
