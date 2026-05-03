package com.example.ecofruit.ui.data.repository

import android.util.Log
import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.data.constants.MessageStatus
import com.example.ecofruit.ui.data.constants.toEmoji
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.data.model.Product
import com.example.ecofruit.ui.model.Conversation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository private constructor() {
    private val TAG = "ChatRepository"
    private val db = FirebaseFirestore.getInstance()
    private val conversationsCollection = db.collection("conversations")

    fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val subscription = conversationsCollection
            .whereArrayContains("participantsId", userId)
            .whereNotEqualTo("lastMessage", null )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to conversations", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val conversations = snapshot.toObjects(Conversation::class.java)
                    trySend(conversations)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getConversationById(conversationId: String): Flow<Conversation?> = callbackFlow {
        val subscription = conversationsCollection.document(conversationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to conversation $conversationId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val conversation = snapshot.toObject(Conversation::class.java)
                    trySend(conversation)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getMessagesFromConversation(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = conversationsCollection.document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages for $conversationId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(messages)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addMessage(message: ChatMessage) {
        try {
            val convRef = conversationsCollection.document(message.conversationId)
            
            // Obtenemos el snapshot directamente para verificar si existe
            val snapshot = convRef.get().await()
            if (!snapshot.exists()) {
                Log.e(TAG, "Conversation ${message.conversationId} does not exist")
                return
            }

            val conversation = snapshot.toObject(Conversation::class.java)
            if (conversation == null) {
                Log.e(TAG, "Failed to deserialize Conversation ${message.conversationId}")
                // Opcional: Intentar enviar el mensaje de todos modos si participantsId está en el snapshot
            }
            
            val participantsId = conversation?.participantsId 
                ?: (snapshot.get("participantsId") as? List<*>)?.filterIsInstance<String>()
                ?: emptyList()

            val messageRef = convRef.collection("messages").document()
            val finalMessage = message.copy(
                id = messageRef.id, 
                status = MessageStatus.SENT, 
                timestamp = System.currentTimeMillis()
            )
            db.runTransaction { transaction ->
                transaction.set(messageRef, finalMessage)

                val otherUserId = conversation!!.primaryOtherUser(message.senderId)

                transaction.update(convRef, mapOf(
                    "lastMessage" to finalMessage,
                    "unreadCount.${otherUserId}" to FieldValue.increment(1)
                ))

            }.await()
            
            Log.d(TAG, "Message sent successfully to Firestore: ${finalMessage.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to Firestore", e)
        }
    }

    fun markConversationAsRead(conversationId: String, userId: String) {
        conversationsCollection.document(conversationId)
            .update("unreadCount.$userId", 0)
            .addOnFailureListener { Log.e(TAG, "Error marking as read for user $userId", it) }
    }

    suspend fun getOrCreateConversation(
        buyerId: String,
        sellerId: String,
        product: Product
    ): String {
        if (buyerId == sellerId) throw Exception("Cannot contact yourself")

        // Buscar conversación existente para este producto y participantes
        val existingSnapshot = conversationsCollection
            .whereEqualTo("productId", product.id)
            .whereArrayContains("participantsId", buyerId)
            .get().await()

        val existing = existingSnapshot.toObjects(Conversation::class.java)
            .find { it.participantsId.contains(sellerId) }

        if (existing != null) return existing.id

        // Crear nueva si no existe
        val newDoc = conversationsCollection.document()
        val conversation = Conversation(
            id = newDoc.id,
            participantsId = listOf(buyerId, sellerId),
            productId = product.id,
            productName = product.name,
            productEmoji = product.type.toEmoji(),
            createdAt = System.currentTimeMillis(),
            conversationTag = mapOf(
                buyerId to ConversationTag.COMPRA,
                sellerId to ConversationTag.VENTA
            ),
            unreadCount = mapOf(
                buyerId to 0,
                sellerId to 0
            )
        )
        newDoc.set(conversation).await()
        return newDoc.id
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
