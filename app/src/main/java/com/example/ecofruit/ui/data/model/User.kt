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
    var following: List<String>,
    var followers: Int,
    val reviewCount: Int,
    val rating: Double
) {
    fun avatarInitials() : String {
        return name.trim()
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    fun hasAvatar(): Boolean = profileImageUrl.isNotBlank()
    fun avatarColor(): Long {
        val palette = listOf(
            0xFF3A7D44L, // verde bosque
            0xFF8B5E3CL, // tierra arcilla
            0xFF5B9E6AL, // verde hoja
            0xFF4A6FA5L, // azul pizarra
            0xFF2D6A4FL, // verde profundo
            0xFF7B4F9EL, // violeta tierra
            0xFF9E5B5BL, // rojo arcilla
            0xFF4F7B9EL, // azul cielo
        )
        val index = (id.hashCode() and Int.MAX_VALUE) % palette.size
        return palette[index]
    }
}

data class FullUserInfo(
    val user: User,
    val products: List<Product>,
    val reviews: List<Review>
)