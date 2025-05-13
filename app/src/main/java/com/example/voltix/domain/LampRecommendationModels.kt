package com.example.voltix.domain

import com.example.voltix.data.entity.JenisRuangan

data class LampRecommendationInput(
    val jenisRuangan: JenisRuangan,
    val panjang: Float,
    val lebar: Float,
    val lampOutputLm: Int,
    val lampEfficacy: Int
)

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