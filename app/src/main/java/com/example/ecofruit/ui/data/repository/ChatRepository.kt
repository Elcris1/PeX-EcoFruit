package com.example.ecofruit.ui.data.repository

import com.example.ecofruit.ui.model.ChatMockData
import com.example.ecofruit.ui.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow

class ChatRepository private constructor() {

    private val TAG = "ChatRepository"

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    init {
        _conversations.value = ChatMockData.conversations
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