package com.example.ecofruit.ui.data.repository

import android.net.Uri
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.model.LocationData
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.*

data class ProductPage(
    val items: List<Product>,
    val lastSnapshot: DocumentSnapshot?,
    val hasMore: Boolean
)

class ProductRepository {

    private val db = Firebase.firestore
    private val productsCollection = db.collection("products")
    private val storage = Firebase.storage
    private val storageRef = storage.reference.child("products")

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

    fun getFavouriteProductsRealtime(userId: String): Flow<Result<List<Product>>> = callbackFlow {
        val subscription = productsCollection
            .whereArrayContains("favouritesList", userId)
            .orderBy("recommendationScore",Query.Direction.DESCENDING)
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

    fun getProductsFromFollowingUsersRealtime(user: User): Flow<Result<List<Product>>> = callbackFlow {
        if (user.following.isEmpty()) {
            trySend(Result.success(emptyList()))
            awaitClose { }
            return@callbackFlow
        }

        val subscription = productsCollection
            .whereIn("userId", user.following)
            .orderBy("createdAt", Query.Direction.DESCENDING)
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

    fun getRecommendedProductsRealtime(): Flow<Result<List<Product>>> = callbackFlow {
        val subscription = productsCollection
            .orderBy("recommendationScore", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
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

    suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        require(product.id.isNotBlank()) { "Product id is required" }
        productsCollection.document(product.id).set(product).await()
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        // First try to read the product to obtain stored image URLs (if any)
        val snapshot = productsCollection.document(productId).get().await()
        val product = snapshot.toObject(Product::class.java)

        // Delete images from Firebase Storage if any URLs are present
        product?.imagesUrl?.forEach { url ->
            if (url.isNotBlank()) {
                // getReferenceFromUrl accepts the full download URL
                val imgRef = storage.getReferenceFromUrl(url)
                imgRef.delete().await()
            }
        }

        // Finally delete the Firestore document
        productsCollection.document(productId).delete().await()
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

    suspend fun searchProducts(
        query: String,
        category: ProductType? = null,
        location: LocationData? = null,
        radiusKm: Double? = null
    ): Result<List<Product>> = runCatching {
        var firestoreQuery: Query = productsCollection
        if (category != null) {
            firestoreQuery = firestoreQuery.whereEqualTo("type", category)
        }

        firestoreQuery = firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
        val snapshot = firestoreQuery.get().await()
        val products = snapshot.toObjects(Product::class.java)
        val filtered = filterProducts(products, query, location, radiusKm)
        filtered
    }

    suspend fun searchProductsPage(
        query: String,
        category: ProductType? = null,
        location: LocationData? = null,
        radiusKm: Double? = null,
        pageSize: Int = 20,
        startAfter: DocumentSnapshot? = null
    ): Result<ProductPage> = runCatching {
        var firestoreQuery: Query = productsCollection
        if (category != null) {
            firestoreQuery = firestoreQuery.whereEqualTo("type", category)
        }

        firestoreQuery = firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
        if (startAfter != null) {
            firestoreQuery = firestoreQuery.startAfter(startAfter)
        }

        val snapshot = firestoreQuery.limit(pageSize.toLong()).get().await()
        val products = snapshot.toObjects(Product::class.java)
        val filtered = filterProducts(products, query, location, radiusKm)
        val last = snapshot.documents.lastOrNull()
        val hasMore = snapshot.size() == pageSize
        ProductPage(filtered, last, hasMore)
    }

    fun searchProductsRealtime(
        query: String,
        category: ProductType? = null,
        location: LocationData? = null,
        radiusKm: Double? = null
    ): Flow<Result<List<Product>>> = callbackFlow {
        var firestoreQuery: Query = productsCollection
        if (category != null) {
            firestoreQuery = firestoreQuery.whereEqualTo("type", category)
        }

        val subscription = firestoreQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.toObjects(Product::class.java)
                val filtered = filterProducts(products, query, location, radiusKm)
                trySend(Result.success(filtered))
            }
        }
        awaitClose { subscription.remove() }
    }

    private fun filterProducts(
        products: List<Product>,
        query: String,
        location: LocationData?,
        radiusKm: Double?
    ): List<Product> {
        val normalizedQuery = query.trim().lowercase()
        val hasLocation = location?.isValid == true && radiusKm != null && radiusKm > 0

        return products.filter { product ->
            val matchesQuery = normalizedQuery.isBlank() ||
                product.name.lowercase().contains(normalizedQuery)

            val matchesLocation = if (hasLocation) {
                val productLocation = product.location
                if (productLocation?.isValid == true) {
                    val distanceKm = haversineKm(
                        location.latitude,
                        location.longitude,
                        productLocation.latitude,
                        productLocation.longitude
                    )
                    distanceKm <= radiusKm
                } else {
                    false
                }
            } else {
                true
            }

            matchesQuery && matchesLocation
        }
    }

    private fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
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
