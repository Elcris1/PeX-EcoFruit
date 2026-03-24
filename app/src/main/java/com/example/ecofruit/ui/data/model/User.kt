package com.example.ecofruit.ui.data.model

import android.location.Location

data class User (
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val profileImageUrl: String,
    val bio: String,
    val location: Location?,
    val isProducer: Boolean,
    val following: List<String>,
    val followers: Int,
    val reviewCount: Int,
    val rating: Double
)