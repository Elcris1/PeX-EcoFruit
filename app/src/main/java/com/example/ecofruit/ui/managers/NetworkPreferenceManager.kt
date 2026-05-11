package com.example.ecofruit.ui.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkPreferenceManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnectionSatisfied = MutableStateFlow(true)
    val isConnectionSatisfied: StateFlow<Boolean> = _isConnectionSatisfied.asStateFlow()

    private var currentCallback: ConnectivityManager.NetworkCallback? = null
    private var isWifiOnly = false

    fun applyWifiOnly(wifiOnly: Boolean) {
        isWifiOnly = wifiOnly
        // Unregister previous callback if exists
        currentCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                // Ignore
            }
        }

        if (wifiOnly) {
            bindToTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            connectivityManager.bindProcessToNetwork(null)
            monitorDefaultNetwork()
        }
    }

    private fun bindToTransport(transport: Int) {
        val request = NetworkRequest.Builder()
            .addTransportType(transport)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)
                _isConnectionSatisfied.value = true
            }
            override fun onLost(network: Network) {
                connectivityManager.bindProcessToNetwork(null)
                _isConnectionSatisfied.value = false
            }
            override fun onUnavailable() {
                connectivityManager.bindProcessToNetwork(null)
                _isConnectionSatisfied.value = false
            }
        }
        currentCallback = callback
        connectivityManager.requestNetwork(request, callback, 10_000)
    }

    private fun monitorDefaultNetwork() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnectionSatisfied.value = true
            }
            override fun onLost(network: Network) {
                _isConnectionSatisfied.value = false
            }
        }
        currentCallback = callback
        connectivityManager.registerDefaultNetworkCallback(callback)
    }
}
