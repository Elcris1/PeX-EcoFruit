package com.example.ecofruit.ui.data.model

import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit

data class Product (
    //TODO: add product categories: fruit, vegetable, meat..
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long,
    val imagesUrl: List<String>,
    val price: Double,
    val unit: ProductUnit,
    val isOrganic: Boolean,
    val type: ProductType,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    var favouritesList: List<String>, //list of user ids that liked the item
    var rating: Double,
    var reviewCount: Int
) {
    fun recommendationScore(): Double {
        val ratingThreshold = 15
        var ratingScore = rating/5
        var reviewNumber = reviewCount
        if ( rating < 2.5) {
            ratingScore = 2.5/-rating
            reviewNumber /= 2
        }

        if (reviewCount < ratingThreshold){
            return ratingScore*2 + reviewNumber/ratingThreshold*8
        }
        return ratingScore*6.5 + reviewNumber/ratingThreshold*3.5
    }
}