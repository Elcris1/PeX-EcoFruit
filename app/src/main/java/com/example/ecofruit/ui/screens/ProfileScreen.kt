package com.example.ecofruit.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.ecofruit.ui.components.RatingRow
import com.example.ecofruit.ui.components.UserImage
import com.example.ecofruit.ui.components.getYear
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.model.RequestUiState


@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👤 Perfil", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Información del usuario")
        }
    }
}


// ─────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────



data class ProfileMenuItem(
    val icon: ImageVector,
    val label: String,
    val description: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false,
    val badge: String? = null,
)

// ─────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────

@Composable
fun ProfileScreen(
    user: User?,
    uiState: RequestUiState<Unit>,
    onEditProfile: () -> Unit,
    onConvertToProducer: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    // Animated avatar ring
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_ring")
    val ringOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (user != null) {
            val menuItems = listOf(
                ProfileMenuItem(
                    icon = Icons.Outlined.Edit,
                    label = stringResource(R.string.profile_edit_profile_title),
                    description = stringResource(R.string.profile_edit_profile_desc),
                    onClick = onEditProfile,
                ),
                ProfileMenuItem(
                    icon = Icons.Outlined.Store,
                    label = if (!user.isProducer) stringResource(R.string.profile_go_pro_title) else stringResource(R.string.profile_go_consumer_title),
                    description = if (!user.isProducer) stringResource(R.string.profile_go_pro_desc) else stringResource(R.string.profile_go_consumer_desc),
                    onClick = onConvertToProducer,
                    badge = if (!user.isProducer) stringResource(R.string.profile_go_pro_badge) else null,
                ),
                ProfileMenuItem(
                    icon = Icons.Outlined.Settings,
                    label = stringResource(R.string.profile_settings_title),
                    description = stringResource(R.string.profile_settings_desc),
                    onClick = onSettings,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .verticalScroll(scrollState),
            ) {

                // ── Header hero ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .drawBehind {
                            // Subtle organic blobs
                            drawCircle(
                                color = colorScheme.primaryContainer.copy(alpha = 0.55f),
                                radius = size.width * 0.55f,
                                center = Offset(size.width * 0.15f, size.height * 0.3f),
                            )
                            drawCircle(
                                color = colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.85f, size.height * 0.7f),
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Avatar
                        Box(contentAlignment = Alignment.Center) {
                            // Animated ring background
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
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
                            // Avatar inner
                            Box(
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                UserImage(user.profileImageUrl, user.name)
                            }

                            // Producer badge
                            if (user.isProducer) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp),
                                    shape = CircleShape,
                                    color = colorScheme.tertiary,
                                    tonalElevation = 2.dp,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Eco,
                                        contentDescription = "Productor",
                                        tint = colorScheme.onTertiary,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // ── User info card ───────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-40).dp),
                    shape = RoundedCornerShape(24.dp),
                    color = colorScheme.surface,
                    shadowElevation = 4.dp,
                    tonalElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Name
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(8.dp))

                        // Email
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MailOutline,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (user.bio.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        RatingRow(user.rating.toFloat(), user.reviewCount)
                        Spacer(Modifier.height(5.dp))
                        HorizontalDivider(color = colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            StatChip(label = stringResource(R.string.followers), value = user.followers.formatCount())
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color = colorScheme.outlineVariant,
                            )
                            StatChip(label = stringResource(R.string.following), value = user.following.count().formatCount())
                            VerticalDivider(
                                modifier = Modifier.height(36.dp),
                                color = colorScheme.outlineVariant,
                            )
                            StatChip(label = stringResource(R.string.from), value = getYear(user.createdAt).toString())
                        }
                    }
                }

                // ── Menu section ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-24).dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.my_account),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                    )

                    menuItems.forEach { item ->
                        ProfileMenuRow(item = item)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Logout – secondary destructive row
                    ProfileMenuRow(
                        item = ProfileMenuItem(
                            icon = Icons.Outlined.Logout,
                            label = stringResource(R.string.log_out),
                            description = stringResource(R.string.profile_log_out_desc),
                            onClick = onLogout,
                            isDestructive = true,
                        ),
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }

        if (uiState is RequestUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Sub-components
// ─────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    val colorScheme = MaterialTheme.colorScheme
    val iconBg = if (item.isDestructive) colorScheme.errorContainer
    else colorScheme.primaryContainer
    val iconTint = if (item.isDestructive) colorScheme.error
    else colorScheme.onPrimaryContainer

    Surface(
        onClick = item.onClick,
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            // Labels
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isDestructive) colorScheme.error else colorScheme.onSurface,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            // Badge
            item.badge?.let { badge ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorScheme.tertiary,
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000     -> "${this / 1_000}K"
    else              -> this.toString()
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    // Replace YourAppTheme with your actual theme
    MaterialTheme {
        ProfileScreen(
            user = User(
                id = "",
                name = "María García",
                email = "maria@ecoapp.es",
                bio = "Apasionada por la agricultura ecológica 🌿 Consumidora responsable desde 2018.",
                isProducer = false,
                followers = 1240,
                following = listOf("u2", "u4"),
                createdAt = 0,
                profileImageUrl = "https://img.freepik.com/psd-gratis/primer-plano-deliciosa-manzana_23-2151868338.jpg?semt=ais_hybrid&w=740&q=80",
                location = null,
                reviewCount = 10,
                rating = 3.0
            ),
            uiState = RequestUiState.Idle(),
            onEditProfile = {},
            onConvertToProducer = {},
            onSettings = {},
            onLogout = {},
        )
    }
}
