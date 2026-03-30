package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.Product

class ProductRepository {

    private var products: List<Product> = emptyList()
    init {
        products = MockData.products
    }
    fun getProducts(): List<Product> = products

    fun getProductsFromUserId(userId: String) = getProducts().filter { it.userId == userId }

    fun addProduct(product: Product) {
        products += product
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductRepository? = null

        fun getInstance(): ProductRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductRepository().also {
                    INSTANCE = it
                }
            }

        }
    }
}