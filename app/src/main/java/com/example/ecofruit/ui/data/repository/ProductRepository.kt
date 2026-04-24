package com.example.ecofruit.ui.data.repository

import android.net.Uri
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = Firebase.firestore
    private val productsCollection = db.collection("products")
    private val storage = Firebase.storage
    private val storageRef = storage.reference.child("products")

    suspend fun getProducts(): Result<List<Product>> = runCatching {
        productsCollection.get().await().toObjects(Product::class.java)
    }

    suspend fun getProductsFromUserId(userId: String): Result<List<Product>> = runCatching {
        productsCollection.whereEqualTo("userId", userId).get().await().toObjects(Product::class.java)
    }

    suspend fun getFavouriteProducts(userId: String): Result<List<Product>> = runCatching {
        productsCollection.whereArrayContains("favouritesList", userId).get().await()
            .toObjects(Product::class.java).sortedByDescending { it.rating }
    }

    suspend fun getProductsFromFollowingUsers(user: User): Result<List<Product>> = runCatching {
        if (user.following.isEmpty()) return@runCatching emptyList()
        productsCollection.whereIn("userId", user.following).get().await()
            .toObjects(Product::class.java).sortedByDescending { it.createdAt }
    }

    suspend fun getRecommendedProducts(): Result<List<Product>> = runCatching {
        productsCollection.get().await().toObjects(Product::class.java)
            .sortedByDescending { it.recommendationScore() }
    }

    suspend fun addProduct(product: Product): Result<Unit> = runCatching {
        val docRef = if (product.id.isEmpty()) productsCollection.document() else productsCollection.document(product.id)
        product.id = docRef.id
        product.createdAt = System.currentTimeMillis()/1000
        docRef.set(product).await()
    }

    suspend fun uploadProductImage(uri: Uri): Result<String> = runCatching {
        val fileName = "product_${System.currentTimeMillis()}_${uri.lastPathSegment}"
        val ref = storageRef.child(fileName)
        ref.putFile(uri).await()
        ref.downloadUrl.await().toString()
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