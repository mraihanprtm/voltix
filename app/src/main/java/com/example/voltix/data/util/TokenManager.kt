package com.example.voltix.data.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(context: Context) {
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "voltix_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_API_TOKEN = "api_token"
    }

    fun saveApiToken(token: String?) {
        sharedPreferences.edit().putString(KEY_API_TOKEN, token).apply()
    }

    fun getApiToken(): String? {
        return sharedPreferences.getString(KEY_API_TOKEN, null)
    }

    fun clearApiToken() {
        sharedPreferences.edit().remove(KEY_API_TOKEN).apply()
    }
}