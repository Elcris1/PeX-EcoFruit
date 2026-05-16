package com.example.ecofruit.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.InteractiveMap
import com.example.ecofruit.ui.components.UserImage
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.toDisplayNameRes
import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.mock.ProductsMockData
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User
import org.maplibre.android.geometry.LatLng
import com.example.ecofruit.ui.data.model.LocationData
import com.example.ecofruit.ui.viewmodels.ProductSearchPagingState
import com.example.ecofruit.ui.theme.EcoFruitTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecofruit.ui.activities.ViewProductActivity
import com.example.ecofruit.ui.activities.ViewProfileActivity
import com.example.ecofruit.ui.managers.LocationHelper

private suspend fun resolveLocationLabel(
    context: Context,
    latLng: LatLng
): String? {
    return LocationHelper.reverseGeocode(context, latLng.latitude, latLng.longitude)
        ?.let { resolved ->
            resolved.city.takeIf { it.isNotBlank() }
                ?: resolved.shortDisplayName.takeIf { it.isNotBlank() }
                ?: resolved.displayName.takeIf { it.isNotBlank() }
        }
}

@Composable
fun SearchScreen(
    searchState: ProductSearchPagingState = ProductSearchPagingState(
        items = ProductsMockData.mockProducts + ProductsMockData.extraProducts,
        hasMore = false
    ),
    users: List<User> = MockData.users,
    onSearch: (
        query: String,
        category: ProductType?,
        location: LocationData?,
        radiusKm: Double?
    ) -> Unit = { _, _, _, _ -> },
    onUserSearch: (query: String) -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }

    var appliedCategory by remember { mutableStateOf<ProductType?>(null) }
    var appliedLocation by remember { mutableStateOf<LatLng?>(null) }
    var appliedLocationLabel by remember { mutableStateOf<String?>(null) }
    var appliedRadiusKm by remember { mutableStateOf(10f) }

    var pendingCategory by remember { mutableStateOf<ProductType?>(null) }
    var pendingLocation by remember { mutableStateOf<LatLng?>(null) }
    var pendingLocationLabel by remember { mutableStateOf<String?>(null) }
    var pendingRadiusKm by remember { mutableStateOf(10f) }

    val locationData = appliedLocation?.let {
        LocationData(latitude = it.latitude, longitude = it.longitude)
    }

    LaunchedEffect(pendingLocation, appliedLocation) {
        pendingLocationLabel = pendingLocation?.let { latLng ->
            resolveLocationLabel(context, latLng)
        }
        appliedLocationLabel = appliedLocation?.let { latLng ->
            resolveLocationLabel(context, latLng)
        }
    }

    LaunchedEffect(query, appliedCategory, appliedLocation, appliedRadiusKm) {
        onSearch(query, appliedCategory, locationData, appliedRadiusKm.toDouble())
        onUserSearch(query)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchHeader(
                query = query,
                onQueryChange = { query = it },
                onClearQuery = { query = "" },
                onOpenFilters = {
                    pendingCategory = appliedCategory
                    pendingLocation = appliedLocation
                    pendingRadiusKm = appliedRadiusKm
                    showFilters = true
                }
            )

            ActiveFiltersSummary(
                selectedCategories = appliedCategory?.let { setOf(it) } ?: emptySet(),
                selectedLocation = appliedLocation,
                selectedLocationLabel = appliedLocationLabel,
                radiusKm = appliedRadiusKm,
                selectedTab = selectedTab
            )

            SearchTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                0 -> SearchProductsContent(
                    products = searchState.items,
                    isLoading = searchState.isLoading,
                    errorMessage = searchState.errorMessage,
                    hasMore = searchState.hasMore,
                    onLoadMore = onLoadMore
                )
                1 -> SearchUsersList(users = users)
            }
        }
    }

    if (showFilters) {
        SearchFiltersDialog(
            selectedCategories = pendingCategory?.let { setOf(it) } ?: emptySet(),
            selectedLocation = pendingLocation,
            selectedLocationLabel = pendingLocationLabel,
            radiusKm = pendingRadiusKm,
            onCategoryToggle = { type ->
                pendingCategory = if (pendingCategory == type) null else type
            },
            onLocationChange = { pendingLocation = it },
            onRadiusChange = { pendingRadiusKm = it },
            onReset = {
                pendingCategory = null
                pendingLocation = null
                pendingLocationLabel = null
                pendingRadiusKm = 10f
            },
            onDismiss = { showFilters = false },
            onApply = {
                appliedCategory = pendingCategory
                appliedLocation = pendingLocation
                appliedLocationLabel = pendingLocationLabel
                appliedRadiusKm = pendingRadiusKm
                showFilters = false
            }
        )
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenFilters: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.search_clear_query)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onOpenFilters,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = stringResource(R.string.search_open_filters)
            )
        }
    }
}

@Composable
private fun ActiveFiltersSummary(
    selectedCategories: Set<ProductType>,
    selectedLocation: LatLng?,
    selectedLocationLabel: String?,
    radiusKm: Float,
    selectedTab: Int
) {
    if (selectedCategories.isEmpty() && selectedLocation == null) return

    val summaryText = buildString {
        if (selectedCategories.isNotEmpty()) {
            append(
                stringResource(
                    R.string.search_active_categories,
                    selectedCategories.size
                )
            )
        }
        if (selectedLocation != null && selectedTab == 0) {
            if (isNotEmpty()) append("  ·  ")
            append(selectedLocationLabel?.takeIf { it.isNotBlank() } ?: stringResource(R.string.search_filters_location))
            append(" - within ")
            append(radiusKm.toInt())
            append(" km")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = summaryText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SearchTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(R.string.search_tab_products),
        stringResource(R.string.search_tab_users)
    )

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                color = MaterialTheme.colorScheme.primary,
                height = 3.dp
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun SearchProductsContent(
    products: List<Product>,
    isLoading: Boolean,
    errorMessage: String?,
    hasMore: Boolean,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            hasMore && !isLoading && products.isNotEmpty() && lastVisible >= products.lastIndex
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    when {
        isLoading && products.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null && products.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (products.isEmpty()) {
                    item { EmptyResultsState() }
                } else {
                    items(products) { product ->
                        ProductResultItem(product = product)
                    }
                }
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchProductsList(products: List<Product>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (products.isEmpty()) {
            EmptyResultsState()
        } else {
            products.forEach { product ->
                ProductResultItem(product = product)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SearchUsersList(users: List<User>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (users.isEmpty()) {
            EmptyResultsState()
        } else {
            users.forEach { user ->
                UserResultItem(user = user)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyResultsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.search_empty_results),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProductResultItem(product: Product) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth().clickable{
            Intent(context, ViewProductActivity::class.java).also {
                it.putExtra("product_id", product.id)
                context.startActivity(it)
            }
        },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = product.imagesUrl.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ecofruit_logo),
                    error = painterResource(R.drawable.ecofruit_logo)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.userName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "%.2f€".format(product.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.End) {

                if (product.reviewCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text =  "(${product.reviewCount}) " + "%.1f".format(product.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(product.type.toDisplayNameRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserResultItem(user: User) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable{
            Intent(context, ViewProfileActivity::class.java).also {
                it.putExtra("user_id", user.id)
                context.startActivity(it)
            }
        },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                UserImage(imageUrl = user.profileImageUrl, name = user.name, modifier = Modifier.fillMaxSize())
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (user.isProducer) {
                        stringResource(R.string.search_user_producer)
                    } else {
                        stringResource(R.string.search_user_consumer)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (user.reviewCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "(${user.reviewCount}) " + "%.1f".format(user.rating) ,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFiltersDialog(
    selectedCategories: Set<ProductType>,
    selectedLocation: LatLng?,
    selectedLocationLabel: String?,
    radiusKm: Float,
    onCategoryToggle: (ProductType) -> Unit,
    onLocationChange: (LatLng) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            color = AlertDialogDefaults.containerColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_filters_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.search_filters_category),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductType.entries.forEach { type ->
                            FilterChipItem(
                                label = stringResource(type.toDisplayNameRes()),
                                selected = type in selectedCategories,
                                onClick = { onCategoryToggle(type) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.search_filters_location),
                        style = MaterialTheme.typography.titleSmall
                    )
                    InteractiveMap(
                        selectedLocation = selectedLocation,
                        onLocationSelected = onLocationChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )

                    if (selectedLocation != null) {
                        Text(
                            text = selectedLocationLabel?.takeIf { it.isNotBlank() } ?: stringResource(R.string.search_filters_location),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.search_filters_radius,
                            radiusKm.toInt()
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = radiusKm,
                        onValueChange = onRadiusChange,
                        valueRange = 1f..50f
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.search_filters_reset))
                    }
                    Button(
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = stringResource(R.string.search_filters_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenPreview() {
    EcoFruitTheme {
        SearchScreen()
    }
}
