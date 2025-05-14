package com.example.voltix.data.entity

data class LampWithPerangkat(
    val lampu: LampuEntity,
    val perangkat: PerangkatEntity
) {
    val lampPowerWatt: Double get() = perangkat.daya.toDouble()

    // jumlah unit lampu
    val jumlah: Int get() = perangkat.jumlah

    // lumen output satu lampu
    val lumenPerLamp: Int get() = lampu.lumen

    // total lumen semua lampu
    val lumenTotal: Int get() = lampu.lumen * jumlah

    // watt per lampu (perangkat.daya adalah watt per lampu)
    val dayaPerLamp: Double get() = perangkat.daya.toDouble()
}