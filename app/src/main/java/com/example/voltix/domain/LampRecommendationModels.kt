package com.example.voltix.domain

import com.example.voltix.data.entity.JenisRuangan

data class LampRecommendationInput(
    val jenisRuangan: JenisRuangan,
    val panjang: Float,
    val lebar: Float,
    val lampOutputLm: Int,
    val lampPowerWatt: Double
)

/**
 * Model hasil baru yang bisa menangani dua skenario:
 * 1. Rekomendasi layak (isFeasible = true)
 * 2. Lampu pilihan tidak efisien (isFeasible = false)
 */
data class SmartRecommendationResult(
    val isFeasible: Boolean,
    val message: String,
    val requiredLux: Int,
    val requiredLumen: Double,
    // 'calculation' akan SELALU berisi hasil perhitungan.
    // 'isFeasible' akan menentukan bagaimana UI menampilkannya.
    val calculation: LampRecommendationResult
)

/**
 * Model yang berisi detail kalkulasi.
 */
data class LampRecommendationResult(
    val numberOfLamps: Int,
    val totalLumen: Int,
    val totalPowerWatt: Double,
    val densityPower: Double,
    val withinStandard: Boolean
)
