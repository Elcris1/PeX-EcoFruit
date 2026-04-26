package com.example.ecofruit.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.InteractiveMap
import com.example.ecofruit.ui.components.UserImage
import com.example.ecofruit.ui.data.model.LocationData
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.managers.LocationHelper
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    editProfileState: RequestUiState<Unit>,
    onSave: (name: String, bio: String, avatarUri: Uri?, location: LocationData?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    val isSaving = editProfileState is RequestUiState.Loading

    // ── Local editable state ──────────────────
    var name by remember { mutableStateOf(user.name) }
    var bio  by remember { mutableStateOf(user.bio)  }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf(user.location) }
    
    var showMapSheet by remember { mutableStateOf(false) }

    // Validation
    val nameError = name.isBlank()
    val hasChanges = name != user.name || bio != user.bio || pickedImageUri != null || selectedLocation != user.location

    // ── Photo picker launcher ─────────────────
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) pickedImageUri = uri
    }

    // Effect to navigate back on success
    LaunchedEffect(editProfileState) {
        if (editProfileState is RequestUiState.Success) {
            onCancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.edit_profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.edit_profile_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .verticalScroll(scrollState),
            ) {

                // ── Hero header ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .drawBehind {
                            drawCircle(
                                color  = colorScheme.primaryContainer.copy(alpha = 0.55f),
                                radius = size.width * 0.55f,
                                center = Offset(size.width * 0.15f, size.height * 0.3f),
                            )
                            drawCircle(
                                color  = colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.85f, size.height * 0.75f),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.clickable {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                colorScheme.primary,
                                                colorScheme.secondary,
                                                colorScheme.tertiary,
                                                colorScheme.primary,
                                            ),
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (pickedImageUri != null) {
                                    UserImage(pickedImageUri.toString(), name)
                                } else {
                                    UserImage(user.profileImageUrl, user.name)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp),
                                shape    = CircleShape,
                                color    = colorScheme.primary,
                                shadowElevation = 3.dp,
                            ) {
                                Icon(
                                    imageVector     = Icons.Outlined.CameraAlt,
                                    contentDescription = stringResource(R.string.edit_profile_tappable_avatar),
                                    tint            = colorScheme.onPrimary,
                                    modifier        = Modifier.padding(6.dp).size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = stringResource(R.string.edit_profile_tappable_avatar),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Form card ─────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-20).dp),
                    shape           = RoundedCornerShape(24.dp),
                    color           = colorScheme.surface,
                    shadowElevation = 4.dp,
                    tonalElevation  = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Text(
                            text       = stringResource(R.string.edit_profile_personal_info),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = colorScheme.onSurface,
                        )

                        OutlinedTextField(
                            value         = name,
                            onValueChange = { name = it },
                            label         = { Text(stringResource(R.string.edit_profile_name_label)) },
                            leadingIcon   = { Icon(Icons.Outlined.Person, null, tint = colorScheme.primary) },
                            isError       = nameError,
                            supportingText = if (nameError) { { Text(stringResource(R.string.edit_profile_name_mandatory), color = colorScheme.error) } } else null,
                            singleLine    = true,
                            shape         = RoundedCornerShape(16.dp),
                            colors        = editFieldColors(colorScheme),
                            modifier      = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value         = bio,
                            onValueChange = { if (it.length <= 200) bio = it },
                            label         = { Text(stringResource(R.string.edit_profile_bio_label)) },
                            leadingIcon   = { Icon(Icons.Outlined.Notes, null, tint = colorScheme.primary) },
                            supportingText = { Text("${bio.length} / 200", modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)) },
                            minLines      = 3,
                            maxLines      = 5,
                            shape         = RoundedCornerShape(16.dp),
                            colors        = editFieldColors(colorScheme),
                            modifier      = Modifier.fillMaxWidth(),
                        )

                        // ── Location Button ───────────────────
                        OutlinedButton(
                            onClick = { showMapSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colorScheme.outline),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(Icons.Outlined.LocationOn, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = selectedLocation?.displayName ?: stringResource(R.string.edit_profile_location_label),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // ── Save button ───────────────────────────
                Button(
                    onClick  = { onSave(name.trim(), bio.trim(), pickedImageUri, selectedLocation) },
                    enabled  = !nameError && hasChanges && !isSaving,
                    shape    = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.edit_profile_save_changes), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // Error display
            if (editProfileState is RequestUiState.Error) {
                Text(
                    text = editProfileState.message,
                    color = colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
                )
            }

            if (isSaving) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showMapSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showMapSheet = false
                if (selectedLocation == null) selectedLocation = user.location
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.8f)) {
                InteractiveMap(
                    selectedLocation = selectedLocation?.let { LatLng(it.latitude, it.longitude) },
                    onLocationSelected = { latLng ->
                        selectedLocation = LocationData(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            city = context.getString(R.string.edit_profile_location_selected)
                        )
                    },
                    initialLatLng = selectedLocation?.let { LatLng(it.latitude, it.longitude) } ?: user.location?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(41.3874, 2.1686),
                )
                
                Button(
                    onClick = {
                        val currentLoc = selectedLocation

                        if (currentLoc != null) {
                            scope.launch {
                                val updatedLocation = LocationHelper.reverseGeocode(
                                    context, currentLoc.latitude, currentLoc.longitude
                                )
                                if (updatedLocation != null) {
                                    selectedLocation = updatedLocation
                                }
                                showMapSheet = false
                            }
                        } else {
                            showMapSheet = false
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.edit_profile_confirm_location))
                }
            }
        }
    }
}

@Composable
private fun editFieldColors(colorScheme: ColorScheme) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor       = colorScheme.primary,
        unfocusedBorderColor     = colorScheme.outline,
        focusedContainerColor    = colorScheme.surface,
        unfocusedContainerColor  = colorScheme.surfaceVariant.copy(alpha = 0.4f),
    )

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditProfileScreenPreview() {
    MaterialTheme {
        EditProfileScreen(
            user = User(
                id               = "",
                name             = "María García",
                email            = "maria@ecoapp.es",
                bio              = "Apasionada por la agricultura ecológica 🌿 Consumidora responsable desde 2018.",
                isProducer       = false,
                followers        = 1240,
                following        = listOf("u2", "u4"),
                createdAt        = 0,
                profileImageUrl  = "",
                location         = null,
                reviewCount      = 10,
                rating           = 3.0,
            ),
            editProfileState = RequestUiState.Idle(),
            onSave    = { _, _, _, _ -> },
            onCancel  = {},
        )
    }
}
