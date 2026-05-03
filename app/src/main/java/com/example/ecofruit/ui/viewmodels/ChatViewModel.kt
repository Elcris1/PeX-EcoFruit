package com.example.ecofruit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.repository.ChatRepository
import com.example.ecofruit.ui.data.repository.UserRepository
import com.example.ecofruit.ui.model.Conversation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
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

    // variables to know the state of the chat
    private val _chatState = MutableStateFlow<RequestUiState<Boolean>>(RequestUiState.Idle())
    val chatState: StateFlow<RequestUiState<Boolean>> = _chatState.asStateFlow()

    // Variables for reactive chat messges
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Variables for conversations state
    private val _conversationsState = MutableStateFlow<RequestUiState<List<ConversationUI>>>(RequestUiState.Idle())
    val conversationsState: StateFlow<RequestUiState<List<ConversationUI>>> = _conversationsState.asStateFlow()
    
    // Variables for reactive conversations
    private val _conversations = MutableStateFlow<List<ConversationUI>>(emptyList())
    val conversations: StateFlow<List<ConversationUI>> = _conversations.asStateFlow()

    private val _conversation = MutableStateFlow<ConversationUI?>(null)
    val conversation: StateFlow<ConversationUI?> = _conversation.asStateFlow()

    private val _contactState = MutableStateFlow<RequestUiState<String>>(RequestUiState.Idle())
    val contactState: StateFlow<RequestUiState<String>> = _contactState.asStateFlow()

    private var conversationsJob: Job? = null
    private var singleConversationJob: Job? = null
    private var messagesJob: Job? = null

    fun getConversationsFromUser(userId: String) {
        conversationsJob?.cancel()
        conversationsJob = viewModelScope.launch {
            _conversationsState.value = RequestUiState.Loading()
            chatRepository.getConversations(userId)
                .catch { e ->
                    _conversationsState.value = RequestUiState.Error(e.message ?: "Error")
                }
                .collect { conversations ->
                    val uiList = conversations.mapNotNull { conversation ->
                        val otherUserId = conversation.primaryOtherUser(userId) ?: return@mapNotNull null
                        val user = userRepository.getUserFromFirestore(otherUserId).getOrNull()
                        if (user != null) ConversationUI(base = conversation, otherUser = user) else null
                    }
                    _conversations.value = uiList
                    _conversationsState.value = RequestUiState.Success(uiList)
                }
        }
    }

    fun getConversationUIFromId(conversationId: String, userId: String) {
        singleConversationJob?.cancel()
        singleConversationJob = viewModelScope.launch {
            _chatState.value = RequestUiState.Loading()
            chatRepository.getConversationById(conversationId).collectLatest { conversation ->
                if (conversation != null) {
                    val otherUserId = conversation.primaryOtherUser(userId)
                    val otherUser = otherUserId?.let { userRepository.getUserFromFirestore(it).getOrNull() }
                    if (otherUser != null) {
                        _conversation.value = ConversationUI(conversation, otherUser)
                        _chatState.value = RequestUiState.Success(true)
                    } else {
                        _chatState.value = RequestUiState.Error("Usuario no encontrado en Firestore")
                    }
                } else {
                    _chatState.value = RequestUiState.Error("Conversación no encontrada")
                }
            }
        }
    }

    fun getMessagesFromConversation(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessagesFromConversation(conversationId)
                .catch { e ->
                    Log.e(TAG, "Error loading messages", e)
                }
                .collect { msgList ->
                    _messages.value = msgList
                }
        }
    }

    fun addMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatRepository.addMessage(message)
        }
    }

    fun markConversationAsRead(conversationId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markConversationAsRead(conversationId, userId)
        }
    }

    fun contactProducer(buyerId: String, sellerId: String, product: Product) {
        viewModelScope.launch {
            _contactState.value = RequestUiState.Loading()
            try {
                val conversationId = chatRepository.getOrCreateConversation(buyerId, sellerId, product)
                _contactState.value = RequestUiState.Success(conversationId)
            } catch (e: Exception) {
                _contactState.value = RequestUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetContactState() {
        _contactState.value = RequestUiState.Idle()
    }
}
