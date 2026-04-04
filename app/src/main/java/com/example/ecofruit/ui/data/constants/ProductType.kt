package com.example.ecofruit.ui.data.constants

import androidx.annotation.StringRes
import com.example.ecofruit.R

enum class ProductType {
    FRUITS, VEGETABLES, CEREAL, TRADITIONAL, //ARTESANAL
    PLANTS, FROM_ANIMAL, OTHER
}
@StringRes
fun ProductType.toDisplayNameRes(): Int {
    return when (this) {
        ProductType.FRUITS -> R.string.product_type_fruits
        ProductType.VEGETABLES -> R.string.product_type_vegetables
        ProductType.CEREAL -> R.string.product_type_cereal
        ProductType.TRADITIONAL -> R.string.product_type_traditional
        ProductType.PLANTS -> R.string.product_type_plants
        ProductType.FROM_ANIMAL -> R.string.product_type_animal
        ProductType.OTHER -> R.string.product_type_other
    }
}