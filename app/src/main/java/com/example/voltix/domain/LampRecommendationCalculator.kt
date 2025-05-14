package com.example.voltix.domain

import javax.inject.Inject

class LampRecommendationCalculator @Inject constructor() {
    fun calculate(input: LampRecommendationInput): LampRecommendationResult {
        val area = input.panjang * input.lebar
        val E = RoomStandard.minLux[input.jenisRuangan]
            ?: error("Standar lux untuk ${input.jenisRuangan} belum diset")
        val Ftotal = E * area / (RoomStandard.Kp * RoomStandard.Kd)
        val N = kotlin.math.ceil(Ftotal / input.lampOutputLm).toInt()
        val totalLumen = N * input.lampOutputLm
        val wattPerLamp = input.lampOutputLm.toDouble() / input.lampEfficacy
        val totalPower = N * wattPerLamp
        val density = totalPower / area
        val maxDens = RoomStandard.maxDensity[input.jenisRuangan]
            ?: error("Standar densitas untuk ${input.jenisRuangan} belum diset")
        val withinStd = density <= maxDens
        return LampRecommendationResult(
            area = area,
            requiredLux = E,
            totalFlux = Ftotal,
            numberOfLamps = N,
            totalLumen = totalLumen,
            totalPowerWatt = totalPower,
            densityPower = density,
            withinStandard = withinStd
        )
    }
}