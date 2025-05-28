package com.example.voltix.data.remote.dto
import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("jenis_listrik")
    val jenisListrik: Int? = null,
    @SerializedName("is_prabayar") // Pastikan nama JSONnya 'is_prabayar'
    val isPrabayar: Boolean? = null,
    val name: String? = null,
    @SerializedName("foto_profil")
    val fotoProfil: String? = null
)