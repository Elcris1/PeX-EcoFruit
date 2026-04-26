package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.data.constants.ReviewType
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val TAG = "ReviewRepository"
    private val db = Firebase.firestore
    private val reviewsCollection = db.collection("reviews")

    suspend fun getReviews(): Result<List<Review>> = runCatching {
        reviewsCollection.get().await().toObjects(Review::class.java)
    }

    suspend fun getReviewsToUser(userId: String): Result<List<Review>> = runCatching {
        reviewsCollection
            .whereEqualTo("reviewType", ReviewType.USER.name)
            .whereEqualTo("dstId", userId)
            .get()
            .await()
            .toObjects(Review::class.java)
    }

    suspend fun addReview(review: Review): Result<Unit> = runCatching {
        val newReviewRef = reviewsCollection.document()
        val finalReview = review.copy(
            id = newReviewRef.id,
            createdAt = System.currentTimeMillis() / 1000
        )
        db.runTransaction { transaction ->
            // 1. Identify the target document (User or Product)
            val targetCollection = if (review.reviewType == ReviewType.USER) "users" else "products"
            val targetDocRef = db.collection(targetCollection).document(review.dstId)

            // 2. Read the current stats from the target
            val targetSnapshot = transaction.get(targetDocRef)
            if (!targetSnapshot.exists()) throw Exception("Target for review not found")

            val currentRating = targetSnapshot.getDouble("rating") ?: 0.0
            val currentCount = targetSnapshot.getLong("reviewCount") ?: 0L

            // 3. Calculate new average
            val newCount = currentCount + 1
            val newAverage = ((currentRating * currentCount) + review.rating) / newCount

            // 4. Perform atomic writes
            transaction.set(newReviewRef, finalReview)
            transaction.update(targetDocRef, mapOf(
                "rating" to newAverage,
                "reviewCount" to newCount
            ))
        }.await()
        newReviewRef.set(finalReview).await()
    }

    companion object {
        @Volatile
        private var INSTANCE: ReviewRepository? = null

        fun getInstance(): ReviewRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReviewRepository().also {
                    INSTANCE = it
                }
            }
        }
    }
}
