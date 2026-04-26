package com.example.ecofruit.ui.managers

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.ecofruit.ui.data.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {
    /**
     * Realiza reverse geocoding para obtener una dirección legible a partir de coordenadas.
     * Devuelve un objeto LocationData con la información encontrada o null si falla.
     */
    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double
    ): LocationData? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            // Usamos la versión síncrona (deprecated pero compatible con APIs antiguas) 
            // dentro de Dispatchers.IO para no bloquear el hilo principal.
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return@withContext LocationData(
                    latitude = latitude,
                    longitude = longitude,
                    address = address.getAddressLine(0) ?: "",
                    city = address.locality ?: address.subAdminArea ?: "",
                    country = address.countryName ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Reverse geocoding failed", e)
        }
        null
    }
}
