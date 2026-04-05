package com.example.ecofruit.ui.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkPreferenceManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun applyWifiOnly(wifiOnly: Boolean) {
        if (wifiOnly) bindToTransport(NetworkCapabilities.TRANSPORT_WIFI)
        else          connectivityManager.bindProcessToNetwork(null) // AUTO
    }

    private fun bindToTransport(transport: Int) {
        val request = NetworkRequest.Builder()
            .addTransportType(transport)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.requestNetwork(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.bindProcessToNetwork(network)
                }
                override fun onLost(network: Network) {
                    connectivityManager.bindProcessToNetwork(null)
                }
                override fun onUnavailable() {
                    connectivityManager.bindProcessToNetwork(null)
                }
            },
            10_000
        )
    }
}