package com.example.ecofruit.ui.data.model

import com.example.ecofruit.ui.data.constants.MessageStatus
import java.time.LocalDateTime

data class ChatMessage(
    var id: String,
    val conversationId: String,
    val senderId: String,           // referencia a User.id
    val text: String,
    val timestamp: LocalDateTime,
    var status: MessageStatus = MessageStatus.SENT,
)

fun ChatMessage.isFromCurrentUser(currentUserId: String): Boolean =
    senderId == currentUserId