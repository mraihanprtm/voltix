package com.example.voltix.domain

import com.example.voltix.data.entity.JenisRuangan
import javax.inject.Inject
import kotlin.math.ceil

class LampRecommendationCalculator @Inject constructor() {

    fun calculate(input: LampRecommendationInput): SmartRecommendationResult {
        // --- Validasi Input Dasar ---
        val area = input.panjang * input.lebar
        if (area <= 0) return SmartRecommendationResult(false, "Luas ruangan tidak valid.", 0, 0.0, emptyResult())
        if (input.lampPowerWatt <= 0) return SmartRecommendationResult(false, "Daya lampu tidak valid.", 0, 0.0, emptyResult())

        val standardLux = RoomStandard.minLux[input.jenisRuangan]
            ?: return SmartRecommendationResult(false, "Standar lux untuk ${input.jenisRuangan.label} tidak ditemukan.", 0, 0.0, emptyResult())
        val maxDensity = RoomStandard.maxDensity[input.jenisRuangan]
            ?: return SmartRecommendationResult(false, "Standar densitas untuk ${input.jenisRuangan.label} tidak ditemukan.", 0, 0.0, emptyResult())

        // --- Perhitungan Inti ---
        val requiredTotalLumen = (standardLux * area) / (RoomStandard.Kp * RoomStandard.Kd)
        val lampEfficacy = if (input.lampPowerWatt > 0) input.lampOutputLm / input.lampPowerWatt else 0.0

        // Hitung jumlah lampu dan konsekuensinya
        val numberOfLamps = ceil(requiredTotalLumen / input.lampOutputLm).toInt()
        val totalLumen = numberOfLamps * input.lampOutputLm
        val totalPower = numberOfLamps * input.lampPowerWatt
        val actualDensity = totalPower / area

        // Buat objek hasil perhitungan
        val calculationResult = LampRecommendationResult(
            numberOfLamps = numberOfLamps,
            totalLumen = totalLumen,
            totalPowerWatt = totalPower,
            densityPower = actualDensity,
            withinStandard = actualDensity <= maxDensity
        )

        // --- Logika Cerdas untuk Menentukan Kelayakan ---
        if (calculationResult.withinStandard) {
            // **SKENARIO IDEAL: Lampu cukup efisien!**
            return SmartRecommendationResult(
                isFeasible = true,
                message = "Rekomendasi optimal ditemukan.",
                requiredLux = standardLux,
                requiredLumen = requiredTotalLumen,
                calculation = calculationResult
            )
        } else {
            // **SKENARIO KURANG IDEAL: Lampu terlalu boros!**
            val message = "Untuk mencapai standar terang, rekomendasi berikut akan sangat MELEBIHI batas hemat energi SNI."
            return SmartRecommendationResult(
                isFeasible = false,
                message = message,
                requiredLux = standardLux,
                requiredLumen = requiredTotalLumen,
                calculation = calculationResult
            )
        }
    }

    private fun emptyResult() = LampRecommendationResult(0, 0, 0.0, 0.0, false)
}