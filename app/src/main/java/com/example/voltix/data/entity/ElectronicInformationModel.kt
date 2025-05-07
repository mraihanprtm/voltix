package com.example.voltix.data.entity

data class ElectronicInformationModel(
    val title: String?,
    val link: String?,
    val displayedLink: String?,
    val snippet: String?,
    val deviceType: String?, // Menambahkan field untuk jenis perangkat
    val wattage: String?     // Menambahkan field untuk daya
)