package com.example.ecofruit.ui.data.repository

import android.net.Uri
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val db = Firebase.firestore
    private val productsCollection = db.collection("products")
    private val storage = Firebase.storage
    private val storageRef = storage.reference.child("products")

    suspend fun getProducts(): Result<List<Product>> = runCatching {
        productsCollection.get().await().toObjects(Product::class.java)
    }

    suspend fun getProduct(productId: String): Result<Product?> = runCatching {
        productsCollection.document(productId).get().await().toObject(Product::class.java)
    }

    fun getProductRealtime(productId: String): Flow<Result<Product?>> = callbackFlow {
        val subscription = productsCollection.document(productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val product = snapshot.toObject(Product::class.java)
                    trySend(Result.success(product))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getProductsFromUserId(userId: String): Result<List<Product>> = runCatching {
        productsCollection.whereEqualTo("userId", userId).get().await().toObjects(Product::class.java)
    }

    suspend fun getFavouriteProducts(userId: String): Result<List<Product>> = runCatching {
        productsCollection.whereArrayContains("favouritesList", userId).get().await()
            .toObjects(Product::class.java).sortedByDescending { it.rating }
    }

    fun getFavouriteProductsRealtime(userId: String): Flow<Result<List<Product>>> = callbackFlow {
        val subscription = productsCollection
            .whereArrayContains("favouritesList", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java).sortedByDescending { it.rating }
                    trySend(Result.success(products))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getProductsFromFollowingUsers(user: User): Result<List<Product>> = runCatching {
        if (user.following.isEmpty()) return@runCatching emptyList()
        productsCollection.whereIn("userId", user.following).get().await()
            .toObjects(Product::class.java).sortedByDescending { it.createdAt }
    }

    fun getProductsFromFollowingUsersRealtime(user: User): Flow<Result<List<Product>>> = callbackFlow {
        if (user.following.isEmpty()) {
            trySend(Result.success(emptyList()))
            awaitClose { }
            return@callbackFlow
        }

        val subscription = productsCollection
            .whereIn("userId", user.following)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java).sortedByDescending { it.createdAt }
                    trySend(Result.success(products))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getRecommendedProducts(): Result<List<Product>> = runCatching {
        productsCollection.get().await().toObjects(Product::class.java)
            .sortedByDescending { it.recommendationScore() }
    }

    fun getRecommendedProductsRealtime(): Flow<Result<List<Product>>> = callbackFlow {
        val subscription = productsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                        .sortedByDescending { it.recommendationScore() }
                    trySend(Result.success(products))
                }
            }
        awaitClose { subscription.remove() }
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

    suspend fun setFavourite(productId: String, userId: String, isFavourite: Boolean): Result<Unit> = runCatching {
        val productRef = productsCollection.document(productId)
        if (isFavourite) {
            productRef.update("favouritesList", FieldValue.arrayUnion(userId)).await()
        } else {
            productRef.update("favouritesList", FieldValue.arrayRemove(userId)).await()
        }
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
