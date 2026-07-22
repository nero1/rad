package com.malawi.radio.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single Malawi radio station.
 * Loaded from assets/stations.json at startup — no backend required.
 */
@Serializable
data class RadioStation(
    val id: String,
    val name: String,
    val frequency: String = "",
    val city: String = "",
    val genre: String = "",
    val language: String = "",
    val streamUrl: String,
    val homepage: String = "",
    val needsVerification: Boolean = false,
    val visible: Boolean = true
)

@Serializable
data class StationsFile(
    val stations: List<RadioStation>
)
