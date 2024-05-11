package com.asessment1.memomind.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(
    name = "settings_preference"
)

class SettingsDataStore(private val context: Context) {

    companion object {
        private val IS_DARK = booleanPreferencesKey("is_dark")
    }

    val themeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK] ?: true
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK] = isDark
        }
    }
}