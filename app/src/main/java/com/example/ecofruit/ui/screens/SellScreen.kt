package com.example.ecofruit.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.ecofruit.R
import com.example.ecofruit.ui.activities.MainScreen
import com.example.ecofruit.ui.components.AnimatedCard
import com.example.ecofruit.ui.components.AnimatedCheck
import com.example.ecofruit.ui.components.CustomTextField
import com.example.ecofruit.ui.components.GeneralButton
import com.example.ecofruit.ui.components.InteractiveMap
import com.example.ecofruit.ui.components.OutlinedGeneralButton
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import com.example.ecofruit.ui.data.constants.toDisplayNameRes
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.theme.EcoFruitTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point


@SuppressLint("UnrememberedMutableState")
@Composable
fun SellScreen(
    onPublish: (Product) -> Unit = {}
) {

    val context = LocalContext.current
    //TAB BAR VARIABLES
    var page by mutableIntStateOf(0)

    //SCREEN 0:
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ProductType>(ProductType.FRUITS) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var isOrganic by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(ProductUnit.KG) }
    var unitExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null)}
    var priceError by remember { mutableStateOf<String?>(null) }

    @SuppressLint("LocalContextGetResourceValueCall")
    fun validateGeneral(): Boolean {
        var valid = true
        if (name.isBlank()) {
            nameError = context.getString(R.string.name_mandatory)
            valid = false
        };
        if (description.isBlank()) {
            descriptionError = context.getString(R.string.sell_description_mandatory)
            valid = false
        };
        if (price.isBlank()) {
            priceError = context.getString(R.string.sell_price_mandatory)
            valid = false
        };

        return valid
    }

    //Screen 1:
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }

    //Screen 2:
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    fun createProduct(): Product {
        return Product(
            name = name,
            description = description,
            imagesUrl = emptyList(),
            price = price.toDouble(),
            unit = selectedUnit,
            isOrganic = isOrganic,
            type = selectedCategory,
            location = selectedLocation
        )
    }

    fun clearValues() {
        //Sc1
        page = 0
        name = ""
        description = ""
        selectedCategory = ProductType.FRUITS
        categoryExpanded = false
        isOrganic = false
        price = ""
        selectedUnit = ProductUnit.KG
        unitExpanded = false

        //Sc1 errors
        nameError = null
        descriptionError = null
        priceError = null

        //Sc2
        images = emptyList()

        //Sc3
        selectedLocation = null

    }


    Scaffold(
        topBar = {
            SellTopBar(
                page = page,
                onBackClick = { page -= 1 },
                onNextClick = {
                    when (page) {
                        0 -> {
                            if (validateGeneral()) page += 1
                        }
                        1 -> {
                            page += 1
                        }
                    }
                },
                onPublish = {
                    onPublish(createProduct())
                    page += 1
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            when (page) {
                0 -> InitialSellingScreen(
                    name = name,
                    onNameChange = {
                        name = it
                        nameError = null
                                   },
                    nameError = nameError,

                    description = description,
                    onDescriptionChange = {
                        description = it
                        descriptionError = null
                                          },
                    descriptionError = descriptionError,

                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    categoryExpanded = categoryExpanded,
                    onCategoryExpandedChange = { categoryExpanded = it },

                    isOrganic = isOrganic,
                    onOrganicChange = { isOrganic = it },

                    price = price,
                    onPriceChange = {
                        price = it
                        priceError= null
                                    },
                    priceError = priceError,

                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it },
                    unitExpanded = unitExpanded,
                    onUnitExpandedChange = { unitExpanded = it }
                )
                1 -> SelectImagesScreen(
                    images = images,
                    onImageChange = {images = it}
                )
                2 -> PublishSellingScreen(
                    selectedLocation = selectedLocation,
                    onLocationSelected = {selectedLocation = it}
                )
                3 -> SuccessScreen(
                    onNewProduct = {
                        page = 0
                        clearValues()
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellTopBar(
    page: Int,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPublish: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = stringResource(R.string.publish_product),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        navigationIcon = {
            if(page > 0){
                OutlinedGeneralButton(
                    text = stringResource(R.string.back),
                    onClick = onBackClick,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        },
        actions = {
            if (page < 2) {
                OutlinedGeneralButton(
                    text = stringResource(R.string.next),
                    onClick = onNextClick
                )
            } else {
                OutlinedGeneralButton(text = stringResource(R.string.publish), onClick = onPublish)

            }

        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InitialSellingScreen(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,

    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?,

    selectedCategory: ProductType,
    onCategoryChange: (ProductType) -> Unit,
    categoryExpanded: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,

    isOrganic: Boolean,
    onOrganicChange: (Boolean) -> Unit,

    price: String,
    onPriceChange: (String) -> Unit,
    priceError: String?,

    selectedUnit: ProductUnit,
    onUnitChange: (ProductUnit) -> Unit,
    unitExpanded: Boolean,
    onUnitExpandedChange: (Boolean) -> Unit
) {

    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(800, easing = EaseOutCubic))
    }



    AnimatedCard(enterAnim) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.sell_general_information),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.primary
            )


            CustomTextField(
                value = name,
                onValueChange = { onNameChange(it) },
                label = stringResource(R.string.sell_product_name_label),
                placeholder = stringResource(R.string.sell_product_name_placeholder),
                icon = Icons.Default.AssignmentInd,
                errorMessage = nameError
            )

            CustomTextField(
                value = description,
                onValueChange = {onDescriptionChange(it) },
                label = stringResource(R.string.sell_product_description_label),
                placeholder =  stringResource(R.string.sell_product_description_placeholder),
                icon = Icons.Default.Info,
                errorMessage = descriptionError
            )

            // Selector de categoría (Dropdown)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { onCategoryExpandedChange(it) }
            ) {
                TextField(
                    value = stringResource(selectedCategory.toDisplayNameRes()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sell_product_category_label)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { onCategoryExpandedChange(false)}
                ) {
                    ProductType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(stringResource(type.toDisplayNameRes())) },
                            onClick = {
                                onCategoryChange(type)
                                onCategoryExpandedChange(false)
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isOrganic,
                    onCheckedChange = { onOrganicChange(it) }
                )
                Text(text = stringResource(R.string.sell_product_organic_label))
            }

            CustomTextField(
                value = price,
                onValueChange = { onPriceChange (it)},
                label = stringResource(R.string.label_price),
                placeholder = stringResource(R.string.placeholder_price),
                icon = Icons.Default.AttachMoney,
                keyboardType = KeyboardType.Number,
                errorMessage = priceError
            )

            // ⚖️ Unidad
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { onUnitExpandedChange(it) }
            ) {
                TextField(
                    value = stringResource(selectedUnit.toDisplayNameRes()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.unit)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { onUnitExpandedChange(false) }
                ) {
                    ProductUnit.values().forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.toDisplayNameRes())) },
                            onClick = {
                                onUnitChange(it)
                                onUnitExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }
    }


}



@Composable
private fun SelectImagesScreen(
    images: List<Uri> = emptyList(),
    onImageChange:(List<Uri>) -> Unit = {}
) {


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        onImageChange(uris.take(6)) // seguridad extra
    }

    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(800, easing = EaseOutCubic))
    }



    AnimatedCard(enterAnim) {

        Spacer(Modifier.height(10.dp))


        LazyColumn(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            item {
                Text(
                    text = stringResource(R.string.sell_product_images),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.primary,
                )
            }

            item {
                GeneralButton (
                    text = stringResource(R.string.sell_product_select_images),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

            }

            items(images.chunked(3)) { rowImages ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowImages.forEach { uri ->
                        Box(modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            IconButton(
                                onClick = { onImageChange(images - uri) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                    // Rellena huecos vacíos en la última fila para mantener el grid
                    repeat(3 - rowImages.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))

    }

}

@Composable
private fun PublishSellingScreen(
    selectedLocation: LatLng? = null,
    onLocationSelected: (LatLng) -> Unit = {}
) {


    Column  {
        Text(text = stringResource(R.string.sell_product_location))

        var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }


        InteractiveMap(
            mapLibreMap = mapLibreMap,
            onMapLibreMapChange = {mapLibreMap = it},

            selectedLocation = selectedLocation,
            onLocationSelected = onLocationSelected,

        )
    }

}


@Composable
private fun SuccessScreen(
    onNewProduct: () -> Unit = {}
) {
    val enterAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnim.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    Spacer(Modifier.height(100.dp))
    AnimatedCard(enterAnim) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            AnimatedCheck()

            Text(
                stringResource(R.string.sell_prodcut_posted),
                style = typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
            GeneralButton(
                text = stringResource(R.string.sell_product_post_again),
                onClick = onNewProduct,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LightSell() {
    EcoFruitTheme(darkTheme = false) {
        SellScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DarkSell() {
    EcoFruitTheme(darkTheme = true) {
        SellScreen()
    }
}