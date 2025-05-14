package com.example.voltix.data.entity

data class ElectronicInformationModel(
    val title: String?,
    val link: String?,
    val displayedLink: String?,
    val snippet: String?,
    val deviceType: String?, // Menambahkan field untuk jenis perangkat
    val wattage: String?,     // Menambahkan field untuk daya
    val lumen: String? = null, // Tambahan untuk lampu
    val lampType: String? = null // Tambahan untuk jenis lampu (LED, CFL, dll.)
)