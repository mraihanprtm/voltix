package com.example.voltix.domain

import com.example.voltix.data.entity.JenisRuangan

object RoomStandard {
    val minLux = mapOf(
        JenisRuangan.RuangTamu     to 150,
        JenisRuangan.KamarTidur    to 50,
        JenisRuangan.Dapur         to 250,
        JenisRuangan.KamarMandi    to 200
    )
    val maxDensity = mapOf(
        JenisRuangan.RuangTamu     to 4.41,
        JenisRuangan.KamarTidur    to 6.35,
        JenisRuangan.Dapur         to 7.53,
        JenisRuangan.KamarMandi    to 10.0
    )
    const val Kp = 0.6
    const val Kd = 0.8
}