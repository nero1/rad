package com.malawi.radio.data.repository

import android.content.Context
import com.malawi.radio.data.local.FavoritesStore
import com.malawi.radio.data.model.RadioStation
import com.malawi.radio.data.model.StationsFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Single source of truth for station data.
 * Stations themselves are bundled in assets/stations.json (no network call needed
 * to populate the list). Favorites are persisted locally via DataStore.
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
            cachedStations = parsed.stations
            parsed.stations
        }
    }

    fun favoriteIds(): Flow<Set<String>> = favoritesStore.favoriteIds

    suspend fun toggleFavorite(stationId: String) = favoritesStore.toggleFavorite(stationId)

    /** Emits favorites from most recently favorited to least recently favorited. */
    fun favoriteStations(): Flow<List<RadioStation>> {
        return favoritesStore.favoriteIdsByRecency.combine(
            kotlinx.coroutines.flow.flow { emit(getAllStations()) }
        ) { favoriteIdsByRecency, all ->
            val stationsById = all.associateBy { it.id }
            favoriteIdsByRecency.mapNotNull { stationsById[it] }
        }
    }
}
