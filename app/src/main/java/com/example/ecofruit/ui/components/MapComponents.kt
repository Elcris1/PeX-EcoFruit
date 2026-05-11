package com.example.ecofruit.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ecofruit.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import java.lang.Exception


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InteractiveMap(
    mapLibreMap: MapLibreMap? = null,
    onMapLibreMapChange: (MapLibreMap) -> Unit = {},

    selectedLocation: LatLng? = null,
    onLocationSelected: (LatLng) -> Unit,

    initialLatLng: LatLng = LatLng(41.3874, 2.1686),

    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    var locationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    fun checkIsLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    var locationEnabled by remember { mutableStateOf(checkIsLocationEnabled(context)) }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        if (!permissionState.status.isGranted) return
        if (!checkIsLocationEnabled(context)) {
            locationEnabled = false
            return
        }

        locationEnabled = true
        isFetchingLocation = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                isFetchingLocation = false
                if (location != null) {
                    locationLatLng = LatLng(location.latitude, location.longitude)
                }
            }
            .addOnFailureListener {
                isFetchingLocation = false
            }
    }

    // Re-check status when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationEnabled = checkIsLocationEnabled(context)
                if (permissionState.status.isGranted && locationEnabled && locationLatLng == null) {
                    fetchCurrentLocation()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        locationEnabled = checkIsLocationEnabled(context)
        if (result.resultCode == Activity.RESULT_OK) {
            fetchCurrentLocation()
        }
    }

    fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationEnabled = true
            fetchCurrentLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    settingResultRequest.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // Proactively request permissions if not granted
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted && !permissionState.status.shouldShowRationale) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            locationEnabled = checkIsLocationEnabled(context)
            if (locationEnabled) {
                fetchCurrentLocation()
            }
        }
    }

    val height = if (modifier == Modifier) 158.dp else Dp.Unspecified

    Box(
        modifier = modifier
            .then(if (height != Dp.Unspecified) Modifier.fillMaxSize() else Modifier)
    ) {
        if (!permissionState.status.isGranted) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Location permission is required to show your current position on the map.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        } else if (!locationEnabled) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Location services are turned off. Please enable them to see your current position.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { checkLocationSettings() }) {
                    Text("Enable Location")
                }
            }
        } else if (isFetchingLocation && selectedLocation == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            MapLibreImplementation (
                modifier = Modifier.fillMaxSize(),
                initialLatLng = selectedLocation ?: locationLatLng ?: initialLatLng,
                markerLatLng = selectedLocation,
                initialZoom = if (selectedLocation != null || locationLatLng != null) 15.0 else 12.0,
                onMapReady = { map ->
                    onMapLibreMapChange(map)

                },
                onLocationSelected = {latlng ->
                    onLocationSelected(latlng)
                }
            )
            // Coordenadas en pantalla
            selectedLocation?.let { latLng ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📍 %.5f, %.5f".format(latLng.latitude, latLng.longitude),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Instrucción si no hay selección
            if (selectedLocation == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.map_touch_to_select),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun MapLibreImplementation(
    modifier: Modifier = Modifier,
    styleUrl: String = "https://tiles.openfreemap.org/styles/liberty",
    initialLatLng: LatLng = LatLng(41.3874, 2.1686),
    markerLatLng: LatLng? = null,
    initialZoom: Double = 9.0,
    onMapReady: (MapLibreMap) -> Unit = {},
    onLocationSelected: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapReference by remember { mutableStateOf<MapLibreMap?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(markerLatLng) }

    val mapView = remember {
        MapLibre.getInstance(context)

        MapView(context).apply {
            getMapAsync { map ->
                mapReference = map
                map.setStyle(styleUrl) {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(initialLatLng)
                        .zoom(initialZoom)
                        .build()
                }

                // Listener de tap
                map.addOnMapClickListener { latLng ->
                    selectedLatLng = latLng
                    onLocationSelected(latLng)
                    true // consumir el evento
                }
            }
            
            // Fix gesture conflict with BottomSheet/Scrollable containers
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false // Important: return false to let MapView handle the event
            }
        }
    }

    LaunchedEffect(selectedLatLng) {
        val map = mapReference ?: return@LaunchedEffect
        val latLng = selectedLatLng ?: return@LaunchedEffect

        map.getStyle { style ->
            val point = Point.fromLngLat(latLng.longitude, latLng.latitude)
            val feature = Feature.fromGeometry(point)

            // Eliminar capa y fuente anteriores si existen
            style.removeLayer("selected-marker-layer")
            style.removeSource("selected-marker-source")

            // Añadir nueva fuente y capa
            style.addSource(GeoJsonSource("selected-marker-source", feature))
            style.addLayer(
                CircleLayer("selected-marker-layer", "selected-marker-source")
                    .withProperties(
                        PropertyFactory.circleRadius(10f),
                        PropertyFactory.circleColor("#FF5722"),
                        PropertyFactory.circleStrokeWidth(2f),
                        PropertyFactory.circleStrokeColor("#FFFFFF")
                    )
            )
        }
    }

    // Sincronizar el ciclo de vida de Compose con MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Botones de zoom
        ZoomControls(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            onZoomIn = {
                mapReference?.let { map ->
                    val current = map.cameraPosition.zoom
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .zoom(current + 1)
                                .build()
                        )
                    )
                }
            },
            onZoomOut = {
                mapReference?.let { map ->
                    val current = map.cameraPosition.zoom
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .zoom(current - 1)
                                .build()
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun ZoomControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ZoomButton(icon = Icons.Default.Add, contentDescription = "Zoom in", onClick = onZoomIn)
        ZoomButton(icon = Icons.Default.Remove, contentDescription = "Zoom out", onClick = onZoomOut)
    }
}

@Composable
private fun ZoomButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
