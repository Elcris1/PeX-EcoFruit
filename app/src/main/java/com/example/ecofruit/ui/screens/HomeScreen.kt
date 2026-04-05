package com.example.ecofruit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecofruit.ui.components.UserImage
import com.example.ecofruit.ui.data.mock.ChatMockData
import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.mock.ProductsMockData.allProducts
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.constants.toDisplayNameRes


import com.example.ecofruit.ui.theme.EcoFruitTheme
//TODO: Cambiar el unit mostrado en el producto por los valores locales
//TODO: poder clicar al perfil del usuario para visitarlo
//TODO: poder clicar al producto para verlo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User?,
    recommendedProducts: List<Product> = emptyList(),
    followedProducerProducts: List<Product> = emptyList(),
    favouriteProducts: List<Product> = emptyList(),
    onProductClick: (Product) -> Unit = {},
    onCartClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onCartClick = onCartClick,
                    onNotificationsClick = onNotificationsClick,
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // ── Search bar ──
                item {
                    SearchBar(onClick = onSearchClick)
                }

                // ── Banner / hero ──
                //item {
                  //  SeasonBanner()
                //}
                if (currentUser!= null) {
                    // ── Sección: Productos Recomendados ──
                    item {
                        ProductSection(
                            title = stringResource(R.string.home_recommended_header),
                            subtitle = stringResource(R.string.home_recomended_subheader),
                            icon = Icons.Outlined.Star,
                            products = recommendedProducts,
                            onProductClick = onProductClick,
                            currentUser = currentUser
                        )
                    }

                    // ── Divider ──
                    item { SectionDivider() }

                    // ── Sección: Productores Seguidos ──
                    item {
                        FollowedProducersSection(
                            producerProducts = followedProducerProducts,
                            onProductClick = onProductClick,
                            currentUser = currentUser
                        )
                    }

                    // ── Divider ──
                    item { SectionDivider() }

                    // ── Sección: Favoritos ──
                    item {
                        ProductSection(
                            title = stringResource(R.string.home_favourite_header),
                            subtitle = stringResource(R.string.home_favourite_subheader),
                            icon = Icons.Filled.Favorite,
                            iconTint = MaterialTheme.colorScheme.error,
                            products = favouriteProducts,
                            onProductClick = onProductClick,
                            currentUser = currentUser
                        )
                    }
                }

            }
        }

}

// ─────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌿",
                    fontSize = 22.sp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ─────────────────────────────────────────────
//  Search Bar
// ─────────────────────────────────────────────

@Composable
private fun SearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.home_search_bar),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Season Banner
// ─────────────────────────────────────────────

@Composable
private fun SeasonBanner() {
    val gradient = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Column {
            Text(
                text = "Temporada de primavera 🌸",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Frutas y verduras frescas\nrecién cosechadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
            ) {
                Text(
                    text = "Ver novedades →",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        // Decorative emoji, top-right
        Text(
            text = "🥦🍓🥕",
            fontSize = 36.sp,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

// ─────────────────────────────────────────────
//  Generic product section
// ─────────────────────────────────────────────

private const val COLLAPSED_ITEM_COUNT = 4

@Composable
private fun ProductSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    currentUser: User,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            icon = icon,
            iconTint = iconTint,
            itemCount = products.size,
            expanded = expanded,
            onToggle = { expanded = !expanded },
        )

        val displayed = if (expanded) products else products.take(COLLAPSED_ITEM_COUNT)

        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "product_section_$title",
        ) { isExpanded ->
            if (isExpanded) {
                // Grid layout when expanded
                ExpandedProductGrid(
                    products = products,
                    onProductClick = onProductClick,
                    currentUser = currentUser,
                )
            } else {
                // Horizontal scroll when collapsed
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = displayed, key = { it.id }) { product ->
                        ProductCard(product = product, onClick = { onProductClick(product) }, currentUserId = currentUser.id)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Followed producers section
// ─────────────────────────────────────────────

@Composable
private fun FollowedProducersSection(
    producerProducts: List<Product>,
    currentUser: User,
    onProductClick: (Product) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.home_your_producers),
            subtitle = stringResource(R.string.home_producers_news),
            icon = Icons.Outlined.Person,
            itemCount = producerProducts.size,
            expanded = expanded,
            onToggle = { expanded = !expanded },
        )

        val displayed = if (expanded) producerProducts
        else producerProducts.take(COLLAPSED_ITEM_COUNT)

        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = stringResource(R.string.followed_producers_section_label),
        ) { isExpanded ->
            if (isExpanded) {
                ExpandedProductGrid(
                    products = producerProducts,
                    onProductClick = onProductClick,
                    showProducerBadge = true,
                    currentUser = currentUser,
                    isFollowing = true
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = displayed, key = { it.id }) { pp ->
                        ProducerProductCard(
                            producerProduct = pp,
                            onClick = { onProductClick(pp) },
                            currentUser = currentUser
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Section header with "ver más" button
// ─────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    itemCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon bubble
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // "Ver más" / "Ver menos" toggle button
        AnimatedContent(
            targetState = expanded,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = stringResource(R.string.toggle_btn_label),
        ) { isExpanded ->
            TextButton(
                onClick = onToggle,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (isExpanded) stringResource(R.string.home_show_less) else stringResource(R.string.home_show_more, itemCount),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp
                    else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Expanded grid layout
// ─────────────────────────────────────────────

@Composable
private fun ExpandedProductGrid(
    products: List<Product>,
    currentUser: User,
    isFollowing: Boolean = false,
    onProductClick: (Product) -> Unit,
    showProducerBadge: Boolean = false,
    producerAvatars: Map<Int, String> = emptyMap(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowProducts.forEach { product ->
                    if (isFollowing) {
                        ProducerProductCard(
                            producerProduct = product,
                            currentUser = currentUser,
                            onClick = {onProductClick(product)},
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            modifier = Modifier.weight(1f),
                            currentUserId = currentUser.id
                        )
                    }

                }
                // If odd number of items, fill space
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ─────────────────────────────────────────────
//  Product Card
// ─────────────────────────────────────────────

@Composable
fun ProductCard(
    product: Product,
    currentUserId: String,
    isFollowing: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFavorite by remember { mutableStateOf(currentUserId in product.favouritesList) }

    val cardWidth = if (modifier == Modifier) 158.dp else Dp.Unspecified
    //val rounded = product.userId in currentUser.following
    val shape: Shape = if (!isFollowing) {
        RoundedCornerShape(18.dp)
    } else {
        RectangleShape
    }
    Surface(
        modifier = modifier
            .then(if (cardWidth != Dp.Unspecified) Modifier.width(cardWidth) else Modifier)
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = shape,
    ) {
        Column {
            // Image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imagesUrl.firstOrNull())
                        .size(300,300)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder =  rememberVectorPainter(Icons.Default.Image),
                    error = rememberVectorPainter(Icons.Default.Image),
                    modifier = Modifier.fillMaxSize()
                )

                // Organic badgeproduct.isOrganic

                if (product.isOrganic) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_eco_tag),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }



                // Favorite button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.home_favourite_content_description),
                        tint = if (isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }


                // Producer avatar (for "followed" section)
                /*
                if (product.userAvatar.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = producerAvatar, fontSize = 14.sp)
                    }
                }

                 */
            }

            // Content area
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isFollowing) {
                    Text(
                        text = product.userName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.2f€".format(product.price),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "/" + stringResource(product.unit.toDisplayNameRes()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    StarRating(rating = product.rating.toFloat())
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Producer Product Card (with producer strip)
// ─────────────────────────────────────────────

@Composable
private fun ProducerProductCard(
    producerProduct: Product,
    currentUser: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cardWidth = if (modifier == Modifier) 158.dp else Dp.Unspecified

    Column(modifier = modifier
        .then(if (cardWidth != Dp.Unspecified) Modifier.width(cardWidth) else Modifier)) {
        // Producer strip header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //Text(text = producerProduct.userAvatar, fontSize = 14.sp)
            //TODO: add userclick to redirec to profile
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                UserImage(producerProduct.userAvatar, producerProduct.userName, modifier = Modifier.size(20.dp).clip(CircleShape))
            }
            Spacer(Modifier.width(5.dp))
            Text(
                text = producerProduct.userName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ProductCard(
            product = producerProduct,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            currentUserId = currentUser.id,
            isFollowing = true
        )
    }
}

// ─────────────────────────────────────────────
//  Star rating
// ─────────────────────────────────────────────

@Composable
private fun StarRating(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

// ─────────────────────────────────────────────
//  Section divider
// ─────────────────────────────────────────────

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────
private val currentUser = MockData.users[0]
private val recommendedProducts = listOf(
    allProducts[0], // Manzanas
    allProducts[5], // Pan
    allProducts[6], // Queso
    allProducts[1]  // Tomates
)
private val followedProducerProducts = allProducts.filter {
    it.userId == "u1" || it.userId == "u3"
}
private val favouriteProducts = allProducts.filter {
    it.favouritesList.contains(currentUser.id)
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EcoFruitTheme() {
        HomeScreen(
            currentUser = currentUser,
            recommendedProducts = recommendedProducts,
            followedProducerProducts = followedProducerProducts,
            favouriteProducts = favouriteProducts
        )
    }

}

@Preview(showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    EcoFruitTheme(darkTheme = true) {
        HomeScreen(
            currentUser = currentUser,
            recommendedProducts = recommendedProducts,
            followedProducerProducts = followedProducerProducts,
            favouriteProducts = favouriteProducts
        )
    }
}