package com.example.ecofruit.ui.model

import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.data.model.ChatMessage


data class Conversation(
    val id: String,
    val participantsId: List<String>,           // incluye al usuario actual
    var lastMessage: ChatMessage? = null,
    var unreadCount: Int = 0,
    val tag: ConversationTag,
    val productId: String,
    val productName: String,
    val productEmoji: String,
    //val messages: List<ChatMessage> = emptyList(),
) {
    fun otherParticipants(currentUserId: String): List<String> =
        participantsId.filter { id != currentUserId }

    /*
    fun displayName(currentUserId: String): String =
        otherParticipants(currentUserId).joinToString(", ") { it.name }
*/
    fun primaryOtherUser(currentUserId: String): String? =
        participantsId.firstOrNull {it != currentUserId}
}
