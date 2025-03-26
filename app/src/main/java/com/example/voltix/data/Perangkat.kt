package com.example.voltix.data

data class Perangkat(val nama: String, val daya: Int, val durasi: Float)

val samplePerangkat = listOf(
    Perangkat("Kulkas", 150, 24f),
    Perangkat("AC", 900, 8f),
    Perangkat("Lampu", 20, 6f),
    Perangkat("TV", 100, 5f)
)