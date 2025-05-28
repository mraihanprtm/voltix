package com.example.voltix.data.remote.dto // Sesuaikan package

import com.google.gson.annotations.SerializedName

data class FirebaseLoginRequest(
    @SerializedName("firebase_id_token") // Pastikan nama ini sama dengan yang diharapkan Laravel
    val firebaseIdToken: String,
    val name: String? = null // Opsional, diisi saat registrasi
)