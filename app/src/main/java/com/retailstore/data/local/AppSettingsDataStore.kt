package com.retailstore.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_THEME = booleanPreferencesKey("dark_theme")

    val isDarkTheme: Flow<Boolean> = context.appSettingsStore.data.map { it[DARK_THEME] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.appSettingsStore.edit { it[DARK_THEME] = enabled }
    }
}
