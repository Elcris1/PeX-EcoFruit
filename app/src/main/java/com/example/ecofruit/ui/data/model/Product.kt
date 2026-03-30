package com.example.ecofruit.ui.data.model

data class Product (
    val id: String,
    val name: String,
    val description: String,
    val imagesUrl: List<String>,
    val price: Double,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    var favouritesList: List<String>
)