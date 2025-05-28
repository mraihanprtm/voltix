package com.example.voltix.data.util // Sesuaikan package

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences // Untuk keamanan lebih
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "voltix_auth_prefs", // Nama file prefs
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val API_TOKEN = "api_token"
    }

    fun saveApiToken(token: String) {
        prefs.edit().putString(API_TOKEN, token).apply()
    }

    fun getApiToken(): String? {
        return prefs.getString(API_TOKEN, null)
    }

    fun clearApiToken() {
        prefs.edit().remove(API_TOKEN).apply()
    }
}