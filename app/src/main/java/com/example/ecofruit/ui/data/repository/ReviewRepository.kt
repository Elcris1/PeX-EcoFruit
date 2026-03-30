package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.data.mock.MockData
import com.example.ecofruit.ui.data.model.Review
import com.example.ecofruit.ui.data.constants.ReviewType

class ReviewRepository {
    private val TAG = "ReviewRepository"

    private var reviews: List<Review> = emptyList()

    init {
        reviews = MockData.reviews
    }

    fun getReviews(): List<Review> = reviews

    fun getReviewsToUser(userId: String): List<Review> = getReviews().filter { it.reviewType== ReviewType.USER && it.dstId == userId }

    fun addReview(review: Review) {
        reviews += review
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