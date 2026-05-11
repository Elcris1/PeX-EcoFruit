package com.example.ecofruit.ui.model

import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.data.model.ChatMessage

data class Conversation(
    val id: String = "",
    val participantsId: List<String> = emptyList(),           // incluye al usuario actual
    var lastMessage: ChatMessage? = null,
    val unreadCount: Map<String, Int> = emptyMap(),           // map of user id and its unread count
    val conversationTag: Map<String, ConversationTag> = emptyMap(), // map of user id and its tag, ej: user1 is buying user2, user2 is selling user 1
    val productId: String = "",
    val productName: String = "",
    val productEmoji: String = "",
    val createdAt: Long = 0,
) {
    fun otherParticipants(currentUserId: String): List<String> =
        participantsId.filter { it != currentUserId }

    fun primaryOtherUser(currentUserId: String): String? =
        participantsId.firstOrNull { it != currentUserId }
}
