package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User

class ProductRepository {

    private var products: List<Product> = emptyList()
    init {
        products = MockData.products
    }
    fun getProducts(): List<Product> = products

    fun getProductsFromUserId(userId: String) = getProducts().filter { it.userId == userId }

    fun getFavouriteProducts(userId: String): List<Product> {
        return getProducts().filter { userId in it.favouritesList }.sortedByDescending { it.rating }
    }

    fun getProductsFromFollowingUsers(user: User): List<Product> {
        return getProducts().filter { it.userId in user.following }.sortedByDescending { it.createdAt }
    }
    fun getRecommendedProducts(userId: String) : List<Product> {
        return getProducts().sortedByDescending { it.recommendationScore() }
    }

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