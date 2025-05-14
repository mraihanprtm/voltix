package com.example.voltix.domain

import com.example.voltix.data.entity.JenisRuangan

data class LampRecommendationInput(
    val jenisRuangan: JenisRuangan,
    val panjang: Float,
    val lebar: Float,
    val lampOutputLm: Int,
    val lampEfficacy: Int
) {
    // Menghitung daya lampu (dalam watt) berdasarkan output lumen dan efikasi
    val lampPowerWatt: Double get() = lampOutputLm.toDouble() / lampEfficacy
}

data class LampRecommendationResult(
    val area: Float,
    val requiredLux: Int,
    val totalFlux: Double,
    val numberOfLamps: Int,
    val totalLumen: Int,
    val totalPowerWatt: Double,
    val densityPower: Double,
    val withinStandard: Boolean
)