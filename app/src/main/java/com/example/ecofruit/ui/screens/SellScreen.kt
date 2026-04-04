package com.example.ecofruit.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecofruit.R
import com.example.ecofruit.ui.activities.MainScreen
import com.example.ecofruit.ui.components.CustomTextField
import com.example.ecofruit.ui.components.GeneralButton
import com.example.ecofruit.ui.components.OutlinedGeneralButton
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import com.example.ecofruit.ui.data.constants.toDisplayNameRes
import com.example.ecofruit.ui.theme.EcoFruitTheme

@SuppressLint("UnrememberedMutableState")
@Composable
fun SellScreen() {


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

    fun validateGeneral(): Boolean {
        var valid = true
        if (name.isBlank()) {
            nameError = "El nombre no puede estar vació"
            valid = false
        };
        if (description.isBlank()) {
            descriptionError = "La descripcion no puede estar vació"
            valid = false
        };
        if (price.isBlank()) {
            priceError = "El precio no puede estar vacío"
            valid = false
        };

        return valid
    }

    //Screen 1:
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }


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

                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
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
                2 -> PublishSellingScreen()
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
                    text = "Vender producto",
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
                TextButton(
                    onClick = onBackClick
                ) {
                    Text(
                        text = "Atras",
                        color = colorScheme.onSurface
                    )
                }

            }
        },
        actions = {
            if (page < 2) {
                TextButton(
                    onClick = onNextClick
                ) {
                    Text(
                        text = "Siguiente",
                        color = colorScheme.onSurface
                    )
                }
            } else {
                TextButton(
                    onClick = onPublish
                ) {
                    Text("Publicar")
                }
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



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Información general",
            style = MaterialTheme.typography.titleLarge,
            color = colorScheme.primary
        )

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        CustomTextField(
            value = name,
            onValueChange = { onNameChange(it) },
            label = "Nombre",
            placeholder = "Introduce el nombre",
            icon = Icons.Default.Person,
            errorMessage = nameError
        )

        CustomTextField(
            value = description,
            onValueChange = {onDescriptionChange(it) },
            label = "Descripción",
            placeholder = "Introduce la descripción",
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
                label = { Text("Categoría") },
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
            Text(text = "Producto orgánico")
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
                label = { Text("Unidad") },
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text(
                text = "Imagenes para el producto",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.primary
            )
        }


        item {
            GeneralButton (
                text = "Seleccionar imagenes",
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

        // ✅ Grid manual en filas de 3 — sin LazyVerticalGrid anidado
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
                                contentDescription = "Eliminar",
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
}

@Composable
private fun PublishSellingScreen() {
    Text(text = "Pubish product")
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