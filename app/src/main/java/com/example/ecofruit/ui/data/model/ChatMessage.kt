package com.example.ecofruit.ui.data.model

import com.example.ecofruit.ui.data.constants.MessageStatus
import java.util.Date

data class ChatMessage(
    var id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    var status: MessageStatus = MessageStatus.SENT,
)

fun ChatMessage.isFromCurrentUser(currentUserId: String): Boolean =
    senderId == currentUserId
