package com.example.voltix.data.remote.dto
import com.google.gson.annotations.SerializedName

data class UserData(
    val id: Int,
    @SerializedName("firebase_uid")
    val firebaseUid: String,
    val name: String?,
    val email: String,
    @SerializedName("jenis_listrik")
    val jenisListrik: Int?, // Nilai daya
    @SerializedName("is_prabayar")
    val isPrabayar: Boolean?, // Pastikan bisa menerima boolean
    @SerializedName("foto_profil")
    val fotoProfil: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? // Atau tipe yang sesuai
)