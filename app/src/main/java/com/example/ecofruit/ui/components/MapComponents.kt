package com.example.ecofruit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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


@Composable
fun InteractiveMap(
    mapLibreMap: MapLibreMap? = null,
    onMapLibreMapChange: (MapLibreMap) -> Unit = {},

    selectedLocation: LatLng? = null,
    onLocationSelected: (LatLng) -> Unit,

    initialLatLng: LatLng = LatLng(41.3874, 2.1686),

    modifier: Modifier = Modifier
) {
    val height = if (modifier == Modifier) 158.dp else Dp.Unspecified

    Box(
        modifier = modifier
            .then(if (height != Dp.Unspecified) Modifier.fillMaxSize() else Modifier)
    ) {
        MapLibreImplementation (
            modifier = Modifier.fillMaxSize(),
            initialLatLng = initialLatLng,
            initialZoom = 12.0,
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
                    text = "Toca el mapa para seleccionar una ubicación",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
@Composable
private fun MapLibreImplementation(
    modifier: Modifier = Modifier,
    styleUrl: String = "https://tiles.openfreemap.org/styles/liberty",
    initialLatLng: LatLng = LatLng(41.3874, 2.1686),
    initialZoom: Double = 9.0,
    onMapReady: (MapLibreMap) -> Unit = {},
    onLocationSelected: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapReference by remember { mutableStateOf<MapLibreMap?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

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