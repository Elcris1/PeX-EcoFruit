package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.Product

class ProductRepository {
    fun getProducts(): List<Product> = MockData.products

    fun addProduct(product: Product) {

    }
}