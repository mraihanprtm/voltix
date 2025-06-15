package com.example.voltix.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltix.data.entity.RekomendasiPenghematanLampuEntity
import com.example.voltix.data.repository.RekomendasiRepository
import com.example.voltix.data.repository.RuanganAndPerangkatRepository
import com.example.voltix.domain.LampRecommendationCalculator
import com.example.voltix.domain.LampRecommendationInput
import com.example.voltix.domain.SmartRecommendationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RekomendasiViewModel @Inject constructor(
    private val calculator: LampRecommendationCalculator,
    private val rekomRepo: RekomendasiRepository,
    private val perangkatRepo: RuanganAndPerangkatRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _result = MutableLiveData<SmartRecommendationResult?>(null)
    val result: LiveData<SmartRecommendationResult?> = _result

    private val _detail = MutableLiveData<com.example.voltix.data.entity.RekomendasiDetail?>()
    val detail: LiveData<com.example.voltix.data.entity.RekomendasiDetail?> = _detail

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

                // PERBAIKAN: Gunakan 'calculation' sesuai dengan nama properti di SmartRecommendationResult
                if (res.isFeasible) {
                    val entity = RekomendasiPenghematanLampuEntity(
                        userId = userId,
                        ruanganId = ruanganId,
                        lampuId = lampuId
                        // Anda bisa menambahkan kolom baru di Entity nanti dan mengisi datanya dari:
                        // jumlahLampuDirekomendasikan = res.calculation.numberOfLamps,
                        // totalDayaDirekomendasikan = res.calculation.totalPowerWatt
                    )
                    rekomRepo.insertRekomendasi(entity)
                }

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

    /** Bersihkan hasil dan error untuk perhitungan baru. */
    fun resetResult() {
        _result.value = null
        _error.value = null
    }
}