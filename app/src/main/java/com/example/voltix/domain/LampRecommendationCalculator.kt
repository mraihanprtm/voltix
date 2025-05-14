package com.example.voltix.domain

import javax.inject.Inject

class LampRecommendationCalculator @Inject constructor() {

    fun calculate(input: LampRecommendationInput): LampRecommendationResult {
        val area = input.panjang * input.lebar
        val standardLux = RoomStandard.minLux[input.jenisRuangan]
            ?: error("Standar lux untuk ${input.jenisRuangan} belum diset")

        // Total flux yang dibutuhkan
        val totalFlux = (standardLux * area) / (RoomStandard.Kp * RoomStandard.Kd)

        // Jumlah lampu (dibulatkan ke atas)
        val N = kotlin.math.ceil(totalFlux / input.lampOutputLm).toInt()

        // Total lumen & total daya
        val totalLumen = N * input.lampOutputLm
        val totalPower = N * input.lampPowerWatt

        // Densitas daya
        val density = totalPower / area

        val maxDensity = RoomStandard.maxDensity[input.jenisRuangan]
            ?: error("Standar densitas untuk ${input.jenisRuangan} belum diset")
        val withinStd = density <= maxDensity

        return LampRecommendationResult(
            area           = area,
            requiredLux    = standardLux,
            totalFlux      = totalFlux,
            numberOfLamps  = N,
            totalLumen     = totalLumen,
            totalPowerWatt = totalPower,
            densityPower   = density,
            withinStandard = withinStd
        )
    }
}