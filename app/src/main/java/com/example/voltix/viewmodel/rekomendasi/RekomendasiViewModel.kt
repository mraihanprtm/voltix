package com.example.voltix.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity
import com.example.voltix.domain.LampRecommendationCalculator
import com.example.voltix.domain.LampRecommendationInput
import javax.inject.Inject

@HiltViewModel
class RekomendasiViewModel @Inject constructor(
    private val calculator: LampRecommendationCalculator,
    private val rekomRepo: com.example.voltix.data.repository.RekomendasiRepository,
    private val perangkatRepo: com.example.voltix.data.repository.RuanganAndPerangkatRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _result = MutableLiveData<com.example.voltix.domain.LampRecommendationResult?>(null)
    val result: LiveData<com.example.voltix.domain.LampRecommendationResult?> = _result

    private val _detail = MutableLiveData<com.example.voltix.data.entity.RekomendasiDetail?>()
    val detail: LiveData<com.example.voltix.data.entity.RekomendasiDetail?> = _detail

    private val _perangkat = MutableLiveData<com.example.voltix.data.entity.PerangkatEntity?>()
    val perangkat: LiveData<com.example.voltix.data.entity.PerangkatEntity?> = _perangkat

    fun calculateAndSave(
        input: LampRecommendationInput,
        userId: Int?,
        ruanganId: Int,
        lampuId: Int
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val res = calculator.calculate(input)
                _result.value = res

                val entity = RekomendasiPenghematanLampuEntity(
                    userId = userId,
                    ruanganId = ruanganId,
                    lampuId = lampuId
                )
                rekomRepo.insertRekomendasi(entity)

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error saat kalkulasi"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _detail.value = rekomRepo.getRekomendasiDetail(id)
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Gagal memuat detail"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Bersihkan hasil dan error */
    fun resetResult() {
        _result.value = null
        _error.value = null
    }
}