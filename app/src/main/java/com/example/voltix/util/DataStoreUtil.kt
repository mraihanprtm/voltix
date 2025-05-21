package com.example.voltix.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreUtil {
    private const val PREFS_NAME = "voltix_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

    fun isOnboardingCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)] ?: false
        }
    }

    suspend fun saveOnboardingCompleted(context: Context, completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(KEY_ONBOARDING_COMPLETED)] = completed
        }
    }
}