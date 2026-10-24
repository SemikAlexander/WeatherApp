package com.example.weatherapp.api.models.response.geocoding


data class GeoCity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null,
) {
    val displayName: String
        get() = listOfNotNull(
            name.takeIf { it.isNotBlank() },
            state?.takeIf { it.isNotBlank() },
            country?.takeIf { it.isNotBlank() },
        ).joinToString(", ")

    companion object {
        val DEFAULT = GeoCity(
            name = "London",
            lat = 51.5073219,
            lon = -0.1276474,
            country = "GB",
            state = "England",
        )
    }
}
