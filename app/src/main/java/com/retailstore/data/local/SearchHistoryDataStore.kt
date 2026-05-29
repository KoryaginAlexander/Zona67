package com.retailstore.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchHistoryStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

@Singleton
class SearchHistoryDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val HISTORY_KEY = stringPreferencesKey("queries")

    val history: Flow<List<String>> = context.searchHistoryStore.data.map { prefs ->
        prefs[HISTORY_KEY]?.split("|||")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun add(query: String) {
        if (query.isBlank()) return
        val current = history.first().toMutableList()
        current.remove(query)
        current.add(0, query)
        context.searchHistoryStore.edit { it[HISTORY_KEY] = current.take(10).joinToString("|||") }
    }

    suspend fun clear() {
        context.searchHistoryStore.edit { it.remove(HISTORY_KEY) }
    }
}
