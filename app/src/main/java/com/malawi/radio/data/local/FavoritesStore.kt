package com.malawi.radio.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_template_prefs")

private const val FAVORITE_ORDER_SEPARATOR = "\u001F"

/**
 * Lightweight persistence for favorite station IDs, station visibility, recently
 * played stations, and the last station shown in the player.
 */
class FavoritesStore(private val context: Context) {

    companion object {
        private val FAVORITE_IDS = stringSetPreferencesKey("favorite_station_ids")
        private val FAVORITE_IDS_BY_RECENCY = stringPreferencesKey("favorite_station_ids_by_recency")
        private val LAST_STATION_ID = stringPreferencesKey("last_station_id")
        private val RECENT_STATION_IDS = stringPreferencesKey("recent_station_ids")
        private val HIDDEN_STATION_IDS = stringSetPreferencesKey("hidden_station_ids")
        private val VISIBILITY_DEFAULTS_APPLIED = booleanPreferencesKey("visibility_defaults_applied")
        private const val MAX_RECENT_STATIONS = 50
    }

    val favoriteIds: Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[FAVORITE_IDS] ?: emptySet() }

    val favoriteIdsByRecency: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val current = prefs[FAVORITE_IDS] ?: emptySet()
            val savedOrder = prefs[FAVORITE_IDS_BY_RECENCY]
                ?.split(FAVORITE_ORDER_SEPARATOR)
                ?.filter { it.isNotBlank() }
                .orEmpty()

            (savedOrder.filter { it in current } + current.filterNot { it in savedOrder }).distinct()
        }

    val recentStationIds: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[RECENT_STATION_IDS]
                ?.split(FAVORITE_ORDER_SEPARATOR)
                ?.filter { it.isNotBlank() }
                .orEmpty()
        }

    val hiddenStationIds: Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[HIDDEN_STATION_IDS] ?: emptySet() }

    suspend fun applyDefaultHiddenStations(defaultHiddenStationIds: Set<String>) {
        context.dataStore.edit { prefs ->
            if (prefs[VISIBILITY_DEFAULTS_APPLIED] != true) {
                prefs[HIDDEN_STATION_IDS] = (prefs[HIDDEN_STATION_IDS] ?: emptySet()) + defaultHiddenStationIds
                prefs[VISIBILITY_DEFAULTS_APPLIED] = true
            }
        }
    }

    suspend fun toggleFavorite(stationId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS] ?: emptySet()
            val savedOrder = prefs[FAVORITE_IDS_BY_RECENCY]
                ?.split(FAVORITE_ORDER_SEPARATOR)
                ?.filter { it.isNotBlank() }
                .orEmpty()
            val nextOrder: List<String>

            prefs[FAVORITE_IDS] = if (stationId in current) {
                nextOrder = savedOrder.filterNot { it == stationId }
                current - stationId
            } else {
                nextOrder = listOf(stationId) + savedOrder.filterNot { it == stationId }
                current + stationId
            }
            prefs[FAVORITE_IDS_BY_RECENCY] = nextOrder.joinToString(FAVORITE_ORDER_SEPARATOR)
        }
    }

    suspend fun setStationVisible(stationId: String, visible: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[HIDDEN_STATION_IDS] ?: emptySet()
            prefs[HIDDEN_STATION_IDS] = if (visible) current - stationId else current + stationId
        }
    }

    suspend fun setLastStation(stationId: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_STATION_ID] = stationId
            val saved = prefs[RECENT_STATION_IDS]
                ?.split(FAVORITE_ORDER_SEPARATOR)
                ?.filter { it.isNotBlank() }
                .orEmpty()
            prefs[RECENT_STATION_IDS] = (listOf(stationId) + saved.filterNot { it == stationId })
                .take(MAX_RECENT_STATIONS)
                .joinToString(FAVORITE_ORDER_SEPARATOR)
        }
    }

    val lastStationId: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[LAST_STATION_ID] }
}
