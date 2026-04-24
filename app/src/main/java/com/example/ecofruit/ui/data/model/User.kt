package com.example.ecofruit.ui.data.model

import com.google.firebase.firestore.PropertyName

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val profileImageUrl: String = "",
    val bio: String = "",
    val location: LocationData? = null,
    @get:PropertyName("isProducer")
    @get:JvmName("isProducer")
    val isProducer: Boolean = false,
    var following: List<String> = emptyList(),
    var followers: Int = 0,
    val reviewCount: Int = 0,
    val rating: Double = 0.0
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
    var products: List<Product>,
    val reviews: List<Review>
)
