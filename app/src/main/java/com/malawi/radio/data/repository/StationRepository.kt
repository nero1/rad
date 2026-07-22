package com.malawi.radio.data.repository

import android.content.Context
import com.malawi.radio.data.local.FavoritesStore
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.data.model.StationsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Single source of truth for station data.
 * Stations themselves are bundled in assets/stations.json (no network call needed
 * to populate the list). Favorites and user visibility choices are persisted locally.
 */
class StationRepository(
    private val context: Context,
    private val favoritesStore: FavoritesStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    private var cachedStations: List<RadioStation>? = null

    suspend fun getAllStations(): List<RadioStation> = withContext(Dispatchers.IO) {
        cachedStations ?: run {
            val text = context.assets.open("stations.json").bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString(StationsFile.serializer(), text)
            favoritesStore.applyDefaultHiddenStations(parsed.stations.filterNot { it.visible }.map { it.id }.toSet())
            cachedStations = parsed.stations
            parsed.stations
        }
    }

    fun allStationsFlow(): Flow<List<RadioStation>> = flow { emit(getAllStations()) }

    fun favoriteIds(): Flow<Set<String>> = favoritesStore.favoriteIds

    fun hiddenStationIds(): Flow<Set<String>> = favoritesStore.hiddenStationIds

    fun visibleStations(): Flow<List<RadioStation>> = allStationsFlow().combine(hiddenStationIds()) { all, hidden ->
        all.filter { it.id !in hidden }
    }

    suspend fun toggleFavorite(stationId: String) = favoritesStore.toggleFavorite(stationId)

    suspend fun setStationVisible(stationId: String, visible: Boolean) = favoritesStore.setStationVisible(stationId, visible)

    suspend fun recordStationPlayed(stationId: String) = favoritesStore.setLastStation(stationId)

    fun lastStation(): Flow<RadioStation?> = favoritesStore.lastStationId.combine(allStationsFlow()) { stationId, all ->
        all.firstOrNull { it.id == stationId }
    }

    /** Emits favorites from most recently favorited to least recently favorited, excluding hidden stations. */
    fun favoriteStations(): Flow<List<RadioStation>> {
        return favoritesStore.favoriteIdsByRecency.combine(visibleStations()) { favoriteIdsByRecency, visible ->
            val stationsById = visible.associateBy { it.id }
            favoriteIdsByRecency.mapNotNull { stationsById[it] }
        }
    }

    /** Emits recently played stations from most recent to oldest, excluding hidden stations. */
    fun recentStations(): Flow<List<RadioStation>> {
        return favoritesStore.recentStationIds.combine(visibleStations()) { recentIds, visible ->
            val stationsById = visible.associateBy { it.id }
            recentIds.mapNotNull { stationsById[it] }
        }
    }
}
