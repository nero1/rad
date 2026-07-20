package com.malawi.radio.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_template_prefs")

private const val FAVORITE_ORDER_SEPARATOR = "\u001F"

/**
 * Lightweight persistence for favorite station IDs and last-played station.
 * Uses Jetpack DataStore instead of Room since we're only storing simple key/value data —
 * no need for a full SQL database for favorites + a couple of preferences.
 */
class FavoritesStore(private val context: Context) {

    companion object {
        private val FAVORITE_IDS = stringSetPreferencesKey("favorite_station_ids")
        private val FAVORITE_IDS_BY_RECENCY = stringPreferencesKey("favorite_station_ids_by_recency")
        private val LAST_STATION_ID = stringPreferencesKey("last_station_id")
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

    suspend fun setLastStation(stationId: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_STATION_ID] = stationId
        }
    }

    val lastStationId: Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[LAST_STATION_ID] }
}
