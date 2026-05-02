package com.example.ecofruit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import com.example.ecofruit.ui.data.constants.toDisplayNameRes
import com.example.ecofruit.ui.data.constants.toEmoji
import com.example.ecofruit.ui.data.model.LocationData
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.theme.EcoFruitTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.emptyList
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


// ─────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    reviews: List<Review>,
    currentUserId: String = "",
    onBackClick: () -> Unit = {},
    onContactProducer: () -> Unit = {},
    onProducerClick: () -> Unit,
    onToggleFavourite: (Boolean) -> Unit = {},
    onAddReview: (Int, String) -> Unit = { _, _ -> },
    onDeleteReview: (Review) -> Unit = {}
) {
    var isFavourite by remember(product.favouritesList, currentUserId) {
        mutableStateOf(product.favouritesList.contains(currentUserId))
    }

    var showAddReviewDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val imageCollapsed by remember {
        derivedStateOf { scrollState.value > 300 }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scrollable body ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // ── Image Pager ──
            ImagePager(
                imageUrls = product.imagesUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            )

            // ── Content card ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 120.dp)
                ) {

                    // ── Type badge + Organic chip ──
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductTypeBadge(type = product.type)
                        if (product.isOrganic) {
                            OrganicBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Name + Favourite ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FavouriteButton(
                            isFavourite = isFavourite,
                            count = product.favouritesList.size,
                            onClick = {
                                isFavourite = !isFavourite
                                onToggleFavourite(isFavourite)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Price ──
                    PriceRow(price = product.price, unit = product.unit)

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Rating row ──
                    RatingRow(rating = product.rating, reviewCount = product.reviewCount)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Meta info grid ──
                    MetaInfoGrid(product = product)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Description ──
                    ExpandableDescription(description = product.description)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Seller card ──
                    SellerCard(
                        name = product.userName,
                        avatarUrl = product.userAvatar,
                        joinedDate = product.createdAt*1000,
                        onProducerClick = onProducerClick
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Reviews section ──
                    ReviewsSection(
                        reviews = reviews,
                        rating = product.rating,
                        reviewCount = product.reviewCount,
                        currentUserId = currentUserId,
                        onAddReview = { showAddReviewDialog = true },
                        onDeleteReview = onDeleteReview
                    )
                }
            }
        }

        // ── Top app bar (floating) ──
        FloatingTopBar(
            title = if (imageCollapsed) product.name else "",
            onBackClick = onBackClick,
            isCollapsed = imageCollapsed
        )

        // ── Bottom CTA ──
        ContactProducerButton(
            onClick = onContactProducer,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showAddReviewDialog) {
        AddReviewDialog(
            onDismiss = { showAddReviewDialog = false },
            onSubmit = { rating, comment ->
                onAddReview(rating, comment)
                showAddReviewDialog = false
            }
        )
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.product_detail_add_review_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Star rating selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(stringResource(R.string.product_detail_comment_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = { onSubmit(rating, comment) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.publish))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Image Pager
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePager(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    val displayUrls = imageUrls.ifEmpty {
        listOf("placeholder")
    }
    val pagerState = rememberPagerState(pageCount = { displayUrls.size })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction

            val scale = 1f - (pageOffset.absoluteValue * 0.05f).coerceIn(0f, 0.05f)
            val alpha = 1f - (pageOffset.absoluteValue * 0.3f).coerceIn(0f, 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                if (displayUrls[page] == "placeholder") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌱",
                            fontSize = 72.sp
                        )
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayUrls[page])
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen del producto ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )
            }
        }

        // Page indicators
        if (displayUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                displayUrls.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "indicator_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        // Image counter chip
        if (displayUrls.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${displayUrls.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Floating Top Bar
// ─────────────────────────────────────────────────────────────────

@Composable
fun FloatingTopBar(
    title: String,
    onBackClick: () -> Unit,
    isCollapsed: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Back button
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = if (isCollapsed) MaterialTheme.colorScheme.surface
            else Color.Black.copy(alpha = 0.35f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = if (isCollapsed) MaterialTheme.colorScheme.onSurface else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Collapsed title
        AnimatedVisibility(
            visible = isCollapsed,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

// ─────────────────────────────────────────────────────────────────
// Badge components
// ─────────────────────────────────────────────────────────────────

@Composable
fun ProductTypeBadge(type: ProductType) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = type.toEmoji(), fontSize = 14.sp)
            Text(
                text = stringResource(type.toDisplayNameRes()),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun OrganicBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = stringResource(R.string.product_detail_eco_label),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Price Row
// ─────────────────────────────────────────────────────────────────

@Composable
fun PriceRow(price: Double, unit: ProductUnit) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = "%.2f€".format(price),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "/ " + stringResource(unit.toDisplayNameRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Rating Row
// ─────────────────────────────────────────────────────────────────

@Composable
fun RatingRow(rating: Double, reviewCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StarRating(rating = rating, size = 18.dp)
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.product_detail_reviews_count, reviewCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StarRating(
    rating: Double,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            val fill = (rating - (i - 1)).coerceIn(0.0, 1.0).toFloat()
            Icon(
                imageVector = when {
                    fill >= 1f -> Icons.Default.Star
                    fill > 0f -> Icons.Default.StarHalf
                    else -> Icons.Outlined.StarBorder
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(size)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Favourite Button
// ─────────────────────────────────────────────────────────────────

@Composable
fun FavouriteButton(
    isFavourite: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavourite) 1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "fav_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (isFavourite) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavourite) "Quitar de favoritos" else "Añadir a favoritos",
                    tint = if (isFavourite) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Meta Info Grid
// ─────────────────────────────────────────────────────────────────

@Composable
fun MetaInfoGrid(product: Product) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            product.location?.let { loc ->
                MetaChip(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.product_detail_location_label),
                    value = loc.shortDisplayName,
                    modifier = Modifier.weight(1f)
                )
            }
            MetaChip(
                icon = Icons.Default.CalendarToday,
                label = stringResource(R.string.product_detail_published_label),
                value = dateFormat.format(Date(product.createdAt*1000)),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetaChip(
                icon = Icons.Default.Scale,
                label = stringResource(R.string.product_detail_unit_label),
                value = stringResource(product.unit.toDisplayNameRes()),
                modifier = Modifier.weight(1f)
            )
            MetaChip(
                icon = Icons.Default.Category,
                label = stringResource(R.string.product_detail_category_label),
                value = "${product.type.toEmoji()} " + stringResource(product.type.toDisplayNameRes()),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Expandable Description
// ─────────────────────────────────────────────────────────────────

@Composable
fun ExpandableDescription(description: String) {
    var expanded by remember { mutableStateOf(false) }
    val maxLines = if (expanded) Int.MAX_VALUE else 3

    Column {
        Text(
            text = stringResource(R.string.product_detail_description_label),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description.ifBlank { stringResource(R.string.product_detail_no_description) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxLines,
            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            lineHeight = 22.sp
        )
        if (description.length > 120) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (expanded) stringResource(R.string.product_detail_show_less) else stringResource(R.string.product_detail_show_more),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Seller Card
// ─────────────────────────────────────────────────────────────────

@Composable
fun SellerCard(name: String, avatarUrl: String, joinedDate: Long, onProducerClick: () -> Unit = {}) {
    val dateFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }

    Column {
        Text(
            text = stringResource(R.string.product_detail_producer_label),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().clickable{
                onProducerClick()
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar de $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { stringResource(R.string.product_detail_producer_label) },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.product_detail_member_since, dateFormat.format(Date(joinedDate))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Reviews Section
// ─────────────────────────────────────────────────────────────────

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    rating: Double,
    reviewCount: Int,
    currentUserId: String = "",
    onAddReview: () -> Unit,
    onDeleteReview: (Review) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val visibleReviews = if (expanded) reviews else reviews.take(2)

    Column {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.product_detail_reviews_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedButton(
                onClick = onAddReview,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.product_detail_add_review_btn),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rating summary
        if (reviewCount > 0) {
            RatingSummaryCard(reviews = reviews, rating = rating, reviewCount = reviewCount)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Review list
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🌾", fontSize = 32.sp)
                    Text(
                        text = stringResource(R.string.product_detail_be_first_review),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visibleReviews.forEach { review ->
                    ReviewCard(
                        review = review,
                        currentUserId = currentUserId,
                        onDeleteReview = onDeleteReview
                    )
                }
            }

            if (reviews.size > 2) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (expanded) stringResource(R.string.product_detail_show_less_reviews)
                        else stringResource(R.string.product_detail_show_all_reviews, reviews.size),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RatingSummaryCard(reviews: List<Review>, rating: Double, reviewCount: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(rating),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StarRating(rating = rating, size = 16.dp)
                Text(
                    text = stringResource(R.string.product_detail_reviews_count, reviewCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            VerticalDivider(
                modifier = Modifier.height(60.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (star in 5 downTo 1) {
                    val countForStar = reviews.count { it.rating.roundToInt() == star }
                    val progress = if (reviewCount > 0) countForStar.toFloat() / reviewCount else 0f
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$star",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.width(10.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(10.dp)
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    currentUserId: String = "",
    onDeleteReview: (Review) -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cabecera: Avatar, Info y Estrellas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.authorAvatar.isNotBlank()) {
                        AsyncImage(
                            model = review.authorAvatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = review.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.authorName.ifBlank { stringResource(R.string.product_detail_default_username) },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormat.format(Date(review.createdAt*1000)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StarRating(rating = review.rating, size = 14.dp)
            }

            // Cuerpo: Comentario y Botón eliminar alineado
            if (review.description.isNotBlank() || review.userId == currentUserId) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    if (review.description.isNotBlank()) {
                        Text(
                            text = review.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (review.userId == currentUserId) {
                        IconButton(
                            onClick = { onDeleteReview(review) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.product_detail_delete_review_desc),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Bottom CTA Button
// ─────────────────────────────────────────────────────────────────

@Composable
fun ContactProducerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.product_detail_contact_producer),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    val sampleProduct = Product(
        id = "1",
        imagesUrl = emptyList<String>(),
        name = "Tomates Cherry Ecológicos",
        description = "Tomates cherry cultivados sin pesticidas en nuestra finca familiar en el corazón del Penedès. " +
                "Recogidos a mano cada mañana para garantizar la máxima frescura. " +
                "Ideales para ensaladas, tapas y cualquier receta mediterránea.",
        createdAt = (System.currentTimeMillis() - 7 * 24 * 3600 * 1000L)/1000,
        location = LocationData(city = "Vilafranca", country = "Spain", address = "Carrer la marinada"),
        price = 3.50,
        unit = ProductUnit.KG,
        isOrganic = true,
        type = ProductType.VEGETABLES,
        userName = "Can Puigdomènech",
        userAvatar = "",
        favouritesList = listOf("user1", "user2", "user3"),
        rating = 4.5,
        reviewCount = 3,
    )

    // Replace with EcoFruitTheme { ... } in your project
    EcoFruitTheme {
        ProductDetailScreen(
            product = sampleProduct,
            reviews = listOf(
                Review(
                    id = "r1",
                    authorName = "Marta G.",
                    rating = 5.0,
                    description = "¡Increíbles! Los mejores tomates cherry que he probado. Muy dulces y jugosos.",
                    createdAt = (System.currentTimeMillis() - 2 * 24 * 3600 * 1000L)/1000
                ),
                Review (
                    id = "r2",
                    authorName = "Pau M.",
                    rating = 4.0,
                    description = "Muy buen producto. La entrega fue rápida y los tomates llegaron en perfecto estado.",
                    createdAt = (System.currentTimeMillis() - 5 * 24 * 3600 * 1000L)/1000
                ),
                Review(
                    id = "r3",
                    authorName = "Laura B.",
                    rating = 4.5,
                    description = "Excelente calidad para ser ecológicos. Los repetiré seguro.",
                    createdAt = (System.currentTimeMillis() - 10 * 24 * 3600 * 1000L)/1000
                )
            ),
            currentUserId = "user1",
            onProducerClick = {}
        )
    }
}
