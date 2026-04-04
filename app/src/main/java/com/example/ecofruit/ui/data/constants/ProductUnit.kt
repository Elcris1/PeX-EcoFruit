package com.example.ecofruit.ui.data.constants

import com.example.ecofruit.R

enum class ProductUnit {KG, LB, UNIT, LITER}

fun ProductUnit.toDisplayNameRes(): Int {
    return when (this) {
        ProductUnit.KG -> R.string.product_unit_kg
        ProductUnit.LB -> R.string.product_unit_lb
        ProductUnit.UNIT -> R.string.product_unit_unit
        ProductUnit.LITER -> R.string.product_unit_liter
    }
}