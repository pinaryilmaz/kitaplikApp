package com.example.kitaplikapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val KEY_DARK = booleanPreferencesKey("dark_mode")
    private val KEY_PROFILE_URI = stringPreferencesKey("profile_uri")

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK] ?: false
    }

    val profileUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_PROFILE_URI]
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK] = enabled }
    }

    suspend fun setProfileUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri.isNullOrBlank()) prefs.remove(KEY_PROFILE_URI)
            else prefs[KEY_PROFILE_URI] = uri
        }
    }
}
