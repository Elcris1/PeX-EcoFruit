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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.ecofruit.ui.components.RatingRow
import com.example.ecofruit.ui.components.UserImage
import com.example.ecofruit.ui.components.getReadableData
import com.example.ecofruit.ui.components.getYear
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import com.example.ecofruit.ui.data.constants.ReviewType
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.data.model.FullUserInfo
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.R
import com.example.ecofruit.ui.data.model.RequestUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    currentUser: User,
    profileState: RequestUiState<FullUserInfo>,
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onMessage: () -> Unit = {},
    onFollow: (Boolean) -> Unit = {},
    onListingClick: (String) -> Unit = {},
    onFavouriteClick: (Product, String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.view_profile_products), stringResource(R.string.view_profile_reviews))

    val scrollState = rememberScrollState()
    val headerAlpha by remember {
        derivedStateOf { (scrollState.value / 300f).coerceIn(0f, 1f) }
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {

        when (profileState) {
            is RequestUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Loading User Info")
                }
            }
            is RequestUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = profileState.message, color = colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
            }
            is RequestUiState.Success -> {
                val profile = profileState.data
                val isOwnProfile = currentUser.id == profile.user.id

                // ── Scrollable content ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {

                    // ── Hero header with gradient ──
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        colorScheme.primaryContainer,
                                        colorScheme.secondaryContainer,
                                        colorScheme.tertiaryContainer
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                )
                            )
                            // Decorative circles
                            drawCircle(
                                color = colorScheme.primary.copy(alpha = 0.12f),
                                radius = 160f,
                                center = Offset(size.width * 0.85f, size.height * 0.2f)
                            )
                            drawCircle(
                                color = colorScheme.secondary.copy(alpha = 0.10f),
                                radius = 100f,
                                center = Offset(size.width * 0.1f, size.height * 0.9f)
                            )
                        }
                    }

                    // ── Profile card ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-56).dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {

                                // Avatar row
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Avatar

                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            colorScheme.primary,
                                                            colorScheme.secondary
                                                        )
                                                    )
                                                ),

                                            contentAlignment = Alignment.Center
                                        ) {
                                            UserImage(profile.user.profileImageUrl, profile.user.name)

                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Action buttons
                                    if (isOwnProfile) {
                                        OutlinedButton(
                                            onClick = onEditProfile,
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.5.dp, colorScheme.primary),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = colorScheme.primary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                stringResource(R.string.edit),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedIconButton(
                                                onClick = onMessage,
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.5.dp, colorScheme.primary),
                                                colors = IconButtonDefaults.outlinedIconButtonColors(
                                                    contentColor = colorScheme.primary
                                                ),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    Icons.Outlined.MailOutline,
                                                    contentDescription = "Messaje",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            var following by remember {mutableStateOf(currentUser.following.contains(profile.user.id))}

                                            Button(
                                                onClick =
                                                    {
                                                        onFollow(following)
                                                        following = !following
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colorScheme.primary
                                                ),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            ) {

                                                var followingText = stringResource(R.string.view_profile_follow)
                                                if(following) {
                                                    followingText = stringResource(R.string.view_profile_following)
                                                } else {
                                                    followingText = stringResource(R.string.view_profile_follow)
                                                    Icon(
                                                        Icons.Outlined.PersonAdd,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))

                                                }
                                                Text(
                                                    followingText,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Name & username
                                Text(
                                    text = profile.user.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )


                                Spacer(modifier = Modifier.height(10.dp))

                                // Bio
                                Text(
                                    text = profile.user.bio.ifBlank { stringResource(R.string.view_profile_bio_not_defined)},
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Location + member since
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                            //TODO: finish this
                                            Text(
                                                if (profile.user.location != null) "Barcelona - Mocked" else stringResource(R.string.not_defined),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colorScheme.onSurfaceVariant
                                            )


                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            stringResource(R.string.from) + " ${getYear(profile.user.createdAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Rating stars
                                RatingRow(rating = profile.user.rating.toFloat(), count = profile.user.reviewCount)

                            }
                        }
                    }

                    // ── Tabs ──
                    Box(modifier = Modifier.offset(y = (-44).dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = colorScheme.surface,
                            contentColor = colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                    color = colorScheme.primary,
                                    height = 3.dp
                                )
                            },
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        // ── Tab content ──
                        Box(modifier = Modifier.padding(top = 48.dp)) {
                            when (selectedTab) {
                                0 -> ListingsGrid(
                                    userId = currentUser.id,
                                    listings = profile.products,
                                    onListingClick = onListingClick,
                                    onFavouriteClick = onFavouriteClick
                                )
                                1 -> ReviewsList(reviews = profile.reviews)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            else -> Unit
        }

        TopAppBar(
            title = {
                val profile = (profileState as? RequestUiState.Success)?.data
                AnimatedAlphaText(
                    text = profile?.user?.name ?: "",
                    alpha = headerAlpha
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = if (headerAlpha > 0.5f) colorScheme.onSurface else colorScheme.onPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface.copy(alpha = headerAlpha),
                scrolledContainerColor = colorScheme.surface
            ),
            modifier = Modifier.align(Alignment.TopStart).alpha(1f)
        )


    }
}

// ─────────────────────────────────────────────
//  Sub-composables
// ─────────────────────────────────────────────

@Composable
private fun AnimatedAlphaText(text: String, alpha: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}

@Composable
private fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}




@Composable
private fun ListingsGrid(
    userId: String,
    listings: List<Product>,
    onListingClick: (String) -> Unit,
    onFavouriteClick: (Product, String, Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    if (listings.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Inventory2,
            message = stringResource(R.string.view_profile_empty_products)
        )
        return
    }

    Column(
        modifier = Modifier.padding(start = 12.dp, end=12.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listings.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { listing ->
                    ProductCard(
                        product = listing,
                        isFollowing = false,
                        onProductClick = {onListingClick(listing.id)},
                        modifier = Modifier.weight(1f),
                        onFavouriteClick = {isFavorite ->
                            onFavouriteClick(listing, userId, isFavorite)
                        },
                        currentUserId = userId
                    )

                }
                // Fill empty slot if odd count
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ListingCard(
    userId: String,
    listing: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var isFav by remember { mutableStateOf(listing.favouritesList.contains(userId)) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Box {
            Column {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colorScheme.surfaceVariant,
                                    colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        listing.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listing.price.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.primary
                    )
                }
            }

            // Favorite button
            IconButton(
                onClick = { isFav = !isFav },
                modifier = Modifier.align(Alignment.TopEnd).size(36.dp)
            ) {
                Icon(
                    if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favourite",
                    tint = if (isFav) colorScheme.error else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewsList(reviews: List<Review>) {
    val colorScheme = MaterialTheme.colorScheme

    if (reviews.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.RateReview,
            message = stringResource(R.string.view_profile_empty_reviews)
        )
        return
    }

    Column(
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        reviews.forEach { review ->
            ReviewCard(review = review)
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reviewer avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    UserImage(review.authorAvatar, review.authorName)

                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        review.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (i <= review.rating) colorScheme.tertiary else colorScheme.outlineVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            "· ${getReadableData(review.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (review.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    review.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────

private val currentUser = User(
    id = "u3",
    name = "Alex",
    email = "alex@ecofruit.com",
    rating = 4.7,
    reviewCount = 58,
    profileImageUrl = "",
    location = null,
    followers = 10,
    following = listOf("u1", "u2"),
    createdAt = 0,
    isProducer = false,
    bio = "Apasionada del slow fashion y el consumo responsable 🌿 Vendo ropa de calidad que ya no uso.",
)
private val products = listOf(
    Product(id= "1", name= "Chaqueta de lana vintage", description="Chaqueta", imagesUrl = listOf(""), price = 35.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf("u3"), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
    Product(id= "2", name= "Vestido boho floral", description="Chaqueta", imagesUrl = listOf(""), price = 22.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf(), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
    Product(id= "3", name= "Botines de cuero", description="Chaqueta", imagesUrl = listOf(""), price = 45.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf(), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
    Product(id= "4", name= "Bolso", description="Chaqueta", imagesUrl = listOf(""), price = 18.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf("u3"), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
    Product(id= "5", name= "Gorro", description="Chaqueta", imagesUrl = listOf(""), price = 12.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf(), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
    Product(id= "6", name= "Pañuelo", description="Chaqueta", imagesUrl = listOf(""), price = 22.0, userId = "u1", userName = "Laura García", userAvatar = "", favouritesList = listOf(), rating=5.0, reviewCount = 1,  createdAt = 0, unit = ProductUnit.KG, isOrganic = false, type= ProductType.OTHER),
)
private var profile = FullUserInfo(
    user = User(
        id = "u1",
        name = "Laura García",
        email = "laura@ecofruit.com",
        rating = 4.7,
        reviewCount = 58,
        profileImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHRh730XAA9_DpBhSC9_DlqiKXC_xtzU3A&s",
        location = null,
        followers = 10,
        following = listOf("u2"),
        createdAt = 0,
        isProducer = true,
        bio = "Apasionada del slow fashion y el consumo responsable 🌿 Vendo ropa de calidad que ya no uso.",
    ),
    products = products,
    reviews = listOf(
        Review(id = "r1", authorName = "Marta L.", userId = "u5", dstId = "u1", title="Todo perfecto, muy rápida enviando y el artículo tal como se describía. ¡Repetiría!", authorAvatar = "", createdAt = 0, reviewType = ReviewType.USER, rating = 5.0, description = "Repetiria"),
        Review(id = "r2", authorName = "Jordi F.", userId ="u4", dstId= "u1", title="Buen vendedor, trato amable. El producto llegó en buen estado.", authorAvatar = "", createdAt = 0, reviewType = ReviewType.USER, rating = 3.0, description = "Repetiria"),
        Review(id = "r3", authorName = "Sara M.", userId= "u2", dstId = "u1", title="Genial experiencia, muy recomendable.", authorAvatar = "", createdAt = 0, reviewType = ReviewType.USER, rating = 1.0, description = "No")
    )
)
@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    // Replace AppTheme with your actual theme composable
    EcoFruitTheme {
        UserProfileScreen(
            currentUser = currentUser,
            profileState = RequestUiState.Success(profile),
            onFavouriteClick =    { _, _, _ -> },
        )
    }
}
