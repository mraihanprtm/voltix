package com.example.voltix.data.entity

data class LampWithPerangkat(
    val lampu: LampuEntity,
    val perangkat: PerangkatEntity
) {
    // Memudahkan akses di UI
    val jumlah: Int get() = perangkat.jumlah
    val lumenTotal: Int get() = lampu.lumen
    val lumenPerLamp: Int get() = lampu.lumen / perangkat.jumlah
    val dayaPerLamp: Double get() = lampu.lumen.toDouble() / perangkat.jumlah // ini contoh
}