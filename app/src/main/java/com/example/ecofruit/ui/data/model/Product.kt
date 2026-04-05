package com.example.ecofruit.ui.data.model

import com.example.ecofruit.ui.data.constants.ProductType
import com.example.ecofruit.ui.data.constants.ProductUnit
import org.maplibre.android.geometry.LatLng

data class Product (
    var id: String? = "",
    val name: String,
    val description: String,
    val createdAt: Long? = 0,
    val imagesUrl: List<String>,
    var location: LatLng? = null,
    val price: Double,
    val unit: ProductUnit,
    val isOrganic: Boolean,
    val type: ProductType,
    var userId: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var favouritesList: List<String> = emptyList(), //list of user ids that liked the item
    var rating: Double = 0.0,
    var reviewCount: Int = 0
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