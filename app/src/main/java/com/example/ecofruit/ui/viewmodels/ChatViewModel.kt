package com.example.ecofruit.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.repository.ChatRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import com.example.ecofruit.ui.model.Conversation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversationUI(
    val base: Conversation,
    val otherUser: User
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val TAG = "ChatViewModel"

    //variables to know the state of the chat
    private val _chatState = MutableStateFlow <RequestUiState<Boolean>>(RequestUiState.Idle())
    val chatState: StateFlow<RequestUiState<Boolean>> = _chatState.asStateFlow()

    //Variables for reactive chat messges
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    //Variables for conversations state
    private val _conversationsState =   MutableStateFlow<RequestUiState<List<ConversationUI>>>(RequestUiState.Idle())
    val conversationsState: StateFlow<RequestUiState<List<ConversationUI>>> = _conversationsState.asStateFlow()
    //Variables for reactive conversations
    private val _conversations = MutableStateFlow<List<ConversationUI>>(emptyList())
    val conversations: StateFlow<List<ConversationUI>> = _conversations.asStateFlow()
    private val _conversationUpdate = MutableStateFlow<Int>(0)
    val conversationUpdate: StateFlow<Int> = _conversationUpdate.asStateFlow()

    private val _conversation = MutableStateFlow<ConversationUI?>(null)
    val conversation : StateFlow<ConversationUI?> = _conversation.asStateFlow()

    fun getConversationsFromUser(userId: String) {
        viewModelScope.launch {
            _conversationsState.value = RequestUiState.Loading()
            try {
                val conversations = chatRepository.getConversationsFromUser(userId)
                val uiList = conversations.mapNotNull { conversation ->
                    val otherUserId = conversation.primaryOtherUser(userId) ?: return@mapNotNull null

                    val user = userRepository.getUserById(otherUserId) ?: return@mapNotNull null

                    ConversationUI(base = conversation, otherUser = user)

                }
                _conversationsState.value = RequestUiState.Success(uiList)
                _conversations.value = uiList
            } catch (e: Exception) {
                _conversationsState.value = RequestUiState.Error(e.message ?: "Error")
            }

        }
    }
    fun getConversationUIFromId(conversationId: String, userId: String){
        viewModelScope.launch {
            val conversation = chatRepository.getConversationById(conversationId)
            val user = userRepository.getUserById(conversation!!.primaryOtherUser(userId)!!)
            _conversation.value = ConversationUI(conversation, user!!)
            Log.d(TAG, "${_conversation.value}")
        }
    }

    fun getMessagesFromConversation(conversationId: String) {
        viewModelScope.launch {
            _chatState.value = RequestUiState.Loading()
            _messages.value = chatRepository.getMessagesFromConversation(conversationId)
            _chatState.value = RequestUiState.Success(true)
            Log.d(TAG, "${_messages.value}")
        }
    }

    fun addMessage(message: ChatMessage) {
        viewModelScope.launch {
            _messages.value += message
            chatRepository.addMessage(message)
            _conversationUpdate.value += 1
            //getMessagesFromConversation(message.conversationId)
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            chatRepository.markConversationAsRead(conversationId)
            _conversationUpdate.value += 1

        }
    }

    fun getConversationFromId(conversationId: String): Conversation? {
        return chatRepository.getConversationById(conversationId)
    }

}