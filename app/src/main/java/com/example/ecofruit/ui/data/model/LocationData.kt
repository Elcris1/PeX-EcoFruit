package com.example.ecofruit.ui.data.model

data class LocationData(
    /** WGS-84 latitude (−90 to +90). */
    val latitude: Double = 0.0,

    /** WGS-84 longitude (−180 to +180). */
    val longitude: Double = 0.0,

    /**
     * Street-level address line resolved by reverse-geocoding.
     * Example: "Carrer de Mallorca 401"
     */
    val address: String = "",

    /**
     * City / municipality name.
     * Example: "Barcelona"
     */
    val city: String = "",

    /**
     * Country name in the device's locale.
     * Example: "España"
     */
    val country: String = "",
) {
    /**
     * Human-readable location string suitable for UI display.
     * Falls back gracefully when some fields are blank.
     */
    val displayName: String
        get() = listOf(address, city, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")

    /** Whether this location has valid, non-zero coordinates. */
    val isValid: Boolean
        get() = latitude != 0.0 || longitude != 0.0

    companion object {
        /** Sentinel for "no location selected yet". */
        val Empty = LocationData()
    }
}