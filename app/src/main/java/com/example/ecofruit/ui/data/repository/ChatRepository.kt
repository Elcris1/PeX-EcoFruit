package com.example.ecofruit.ui.data.repository

import android.util.Log
import com.example.ecofruit.ui.data.constants.MessageStatus
import com.example.ecofruit.ui.data.mock.ChatMockData
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.model.Conversation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

class ChatRepository private constructor() {
    //todo: make data reactive to changes

    private val TAG = "ChatRepository"

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    init {
        _conversations.value = ChatMockData.conversations
        _messages.value = ChatMockData.messages
    }

    fun getConversationsFromUser(userId: String): List<Conversation> {
        return _conversations.value.filter { it.participantsId.contains(userId) }
    }

    fun getConversations(userId: String): Flow<List<Conversation>> {
        return _conversations.map { conversations ->
            conversations.filter { it.participantsId.contains(userId) }
        }
    }

    fun getConversationById(conversationId: String): Conversation? {
        return _conversations.value.firstOrNull { it.id == conversationId }
    }

    suspend fun getMessagesFromConversation(conversationId: String): List<ChatMessage> {
        delay(100)
        return _messages.value.filter { it.conversationId == conversationId }
    }

    suspend fun addMessage(message: ChatMessage) {
        Log.d(TAG, "Sending message")
        message.id = UUID.randomUUID().toString()
        _messages.update { messageList->
            message.status = MessageStatus.SENT
            getConversationById(message.conversationId)?.let {
                it.lastMessage = message
            }
            messageList + message
        }

    }

     fun markConversationAsRead(conversationId: String) {
         getConversationById(conversationId)?.also {
             it.unreadCount = 0
         }
         getUnreadMessagesFromConversation(conversationId).also {
             it.map {
                 it.status = MessageStatus.READ
             }
         }
    }

    private fun getUnreadMessagesFromConversation(conversationId: String): List<ChatMessage> {
        return _messages.value.filter { it.conversationId == conversationId && it.status != MessageStatus.READ }
    }


    //SINGLETON
    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatRepository().also { INSTANCE = it }
            }
        }
    }
}