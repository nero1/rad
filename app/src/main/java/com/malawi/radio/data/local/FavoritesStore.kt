package com.malawi.radio.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "malawi_radio_prefs")

/**
 * Lightweight persistence for favorite station IDs and last-played station.
 * Uses Jetpack DataStore instead of Room since we're only storing simple key/value data —
 * no need for a full SQL database for favorites + a couple of preferences.
 */
class FavoritesStore(private val context: Context) {

    companion object {
        private val FAVORITE_IDS = stringSetPreferencesKey("favorite_station_ids")
        private val LAST_STATION_ID = stringPreferencesKey("last_station_id")
    }

    val favoriteIds: Flow<Set<String>> =
        context.dataStore.data.map { prefs -> prefs[FAVORITE_IDS] ?: emptySet() }

    suspend fun toggleFavorite(stationId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS] ?: emptySet()
            prefs[FAVORITE_IDS] = if (stationId in current) {
                current - stationId
            } else {
                current + stationId
            }
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
