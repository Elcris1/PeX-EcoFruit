package com.example.ecofruit.ui.model

import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.data.constants.MessageStatus
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.data.model.User
import java.time.LocalDateTime


data class Conversation(
    val id: String,
    val participants: List<User>,           // incluye al usuario actual
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val tag: ConversationTag,
    val productId: String,
    val productName: String,
    val productEmoji: String,
    val messages: List<ChatMessage> = emptyList(),
) {
    fun otherParticipants(currentUserId: String): List<User> =
        participants.filter { it.id != currentUserId }

    fun displayName(currentUserId: String): String =
        otherParticipants(currentUserId).joinToString(", ") { it.name }

    fun primaryOtherUser(currentUserId: String): User? =
        otherParticipants(currentUserId).firstOrNull()
}

// ── User avatar helpers ────────────────────────────────────────────────────
// Tu modelo User usa profileImageUrl; cuando está vacío usamos las iniciales.

fun User.avatarInitials(): String =
    name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

fun User.hasAvatar(): Boolean = profileImageUrl.isNotBlank()

// Color de avatar determinista a partir del id (sin depender de un campo extra)
fun User.avatarColor(): Long {
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

// ── Mock data ──────────────────────────────────────────────────────────────

object ChatMockData {

    val currentUser = User(
        id = "user_me",
        name = "Alex García",
        email = "alex@ecofruit.com",
        createdAt = 1_700_000_000L,
        profileImageUrl = "",
        bio = "Productor ecológico en Barcelona",
        location = null,
        isProducer = true,
        following = listOf("user_marta", "user_jordi"),
        followers = 34,
        reviewCount = 12,
        rating = 4.7,
    )

    private val marta = User(
        id = "user_marta",
        name = "Marta Oliveira",
        email = "marta@ecofruit.com",
        createdAt = 1_690_000_000L,
        profileImageUrl = "",
        bio = "Amante de los productos locales",
        location = null,
        isProducer = false,
        following = listOf("user_me"),
        followers = 10,
        reviewCount = 5,
        rating = 4.2,
    )

    private val jordi = User(
        id = "user_jordi",
        name = "Jordi Puig",
        email = "jordi@ecofruit.com",
        createdAt = 1_680_000_000L,
        profileImageUrl = "",
        bio = "Agricultor de Vic",
        location = null,
        isProducer = true,
        following = emptyList(),
        followers = 22,
        reviewCount = 18,
        rating = 4.9,
    )

    private val laia = User(
        id = "user_laia",
        name = "Laia Ferrer",
        email = "laia@ecofruit.com",
        createdAt = 1_695_000_000L,
        profileImageUrl = "",
        bio = "",
        location = null,
        isProducer = false,
        following = emptyList(),
        followers = 3,
        reviewCount = 2,
        rating = 4.0,
    )

    private val pau = User(
        id = "user_pau",
        name = "Pau Castells",
        email = "pau@ecofruit.com",
        createdAt = 1_685_000_000L,
        profileImageUrl = "",
        bio = "Comprador habitual de mercado",
        location = null,
        isProducer = false,
        following = listOf("user_me", "user_jordi"),
        followers = 8,
        reviewCount = 7,
        rating = 4.5,
    )

    val conversations: List<Conversation> = listOf(

        run {
            val msgs = listOf(
                ChatMessage("m1_001", "conv_001", marta.id, "Hola! Vi tu anuncio de limones ecológicos.", LocalDateTime.now().minusHours(2), MessageStatus.READ),
                ChatMessage("m2_001", "conv_001", currentUser.id, "Hola Marta! Sí, tengo unos 5 kg disponibles recién recogidos.", LocalDateTime.now().minusHours(1).minusMinutes(50), MessageStatus.READ),
                ChatMessage("m3_001", "conv_001", marta.id, "Perfecto, ¿cuánto sería el precio por kilo?", LocalDateTime.now().minusHours(1).minusMinutes(30), MessageStatus.READ),
                ChatMessage("m4_001", "conv_001", currentUser.id, "Son 2,50 € el kg. Son del árbol, sin tratamientos.", LocalDateTime.now().minusHours(1).minusMinutes(10), MessageStatus.READ),
                ChatMessage("m5_001", "conv_001", marta.id, "¡Qué buena pinta! ¿Podrías apartar 3 kg?", LocalDateTime.now().minusMinutes(20), MessageStatus.DELIVERED),
                ChatMessage("m6_001", "conv_001", marta.id, "¿Todavía tienes los limones disponibles?", LocalDateTime.now().minusMinutes(5), MessageStatus.DELIVERED),
            )
            Conversation(id = "conv_001", participants = listOf(currentUser, marta), lastMessage = msgs.last(), unreadCount = 2, tag = ConversationTag.COMPRA, productId = "prod_001", productName = "Limones ecológicos", productEmoji = "🍋", messages = msgs)
        },

        run {
            val msgs = listOf(
                ChatMessage("m1_002", "conv_002", currentUser.id, "Hola Jordi, te escribo por los tomates cherry.", LocalDateTime.now().minusHours(5), MessageStatus.READ),
                ChatMessage("m2_002", "conv_002", jordi.id, "Claro! Tengo dos variedades: amarillos y rojos.", LocalDateTime.now().minusHours(4).minusMinutes(45), MessageStatus.READ),
                ChatMessage("m3_002", "conv_002", currentUser.id, "Me interesan los rojos. ¿Cuánto tienes?", LocalDateTime.now().minusHours(4).minusMinutes(20), MessageStatus.READ),
                ChatMessage("m4_002", "conv_002", jordi.id, "Unos 4 kg. Cosechados esta mañana.", LocalDateTime.now().minusHours(3), MessageStatus.READ),
                ChatMessage("m5_002", "conv_002", currentUser.id, "Perfecto, me los quedo todos. ¿Dónde podemos quedar?", LocalDateTime.now().minusHours(2), MessageStatus.READ),
                ChatMessage("m6_002", "conv_002", jordi.id, "Quedamos el sábado a las 10h entonces.", LocalDateTime.now().minusHours(1), MessageStatus.READ),
            )
            Conversation(id = "conv_002", participants = listOf(currentUser, jordi), lastMessage = msgs.last(), unreadCount = 0, tag = ConversationTag.VENTA, productId = "prod_002", productName = "Tomates cherry", productEmoji = "🍅", messages = msgs)
        },

        run {
            val msgs = listOf(
                ChatMessage("m1_003", "conv_003", laia.id, "Buenas, vi tu anuncio de granola artesanal.", LocalDateTime.now().minusDays(1).minusHours(3), MessageStatus.READ),
                ChatMessage("m2_003", "conv_003", currentUser.id, "Hola Laia! Sí dime, ¿en qué te puedo ayudar?", LocalDateTime.now().minusDays(1).minusHours(2), MessageStatus.READ),
                ChatMessage("m3_003", "conv_003", laia.id, "¿Son sin gluten los productos?", LocalDateTime.now().minusDays(1), MessageStatus.DELIVERED),
            )
            Conversation(id = "conv_003", participants = listOf(currentUser, laia), lastMessage = msgs.last(), unreadCount = 1, tag = ConversationTag.CONSULTA, productId = "prod_003", productName = "Granola artesanal", productEmoji = "🌾", messages = msgs)
        },

        run {
            val msgs = listOf(
                ChatMessage("m1_004", "conv_004", currentUser.id, "Hola Pau! El aceite ya está listo para recoger.", LocalDateTime.now().minusDays(3).minusHours(8), MessageStatus.READ),
                ChatMessage("m2_004", "conv_004", pau.id, "Genial! Paso esta tarde.", LocalDateTime.now().minusDays(3).minusHours(6), MessageStatus.READ),
                ChatMessage("m3_004", "conv_004", pau.id, "Muchas gracias, fue todo perfecto 🌿", LocalDateTime.now().minusDays(3), MessageStatus.READ),
            )
            Conversation(id = "conv_004", participants = listOf(currentUser, pau), lastMessage = msgs.last(), unreadCount = 0, tag = ConversationTag.COMPRA, productId = "prod_004", productName = "Aceite de oliva", productEmoji = "🫒", messages = msgs)
        },
    )
}