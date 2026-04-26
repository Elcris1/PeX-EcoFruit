package com.example.ecofruit.ui.data.model

import com.example.ecofruit.ui.data.constants.ReviewType

data class Review (
    val id: String = "",
    val userId: String = "",
    val dstId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val rating: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val reviewType: ReviewType = ReviewType.USER,
    val createdAt: Long = 0L
)
