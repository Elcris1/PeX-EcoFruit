package com.example.ecofruit.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecofruit.ui.theme.EcoFruitTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.ChatMessage
import com.example.ecofruit.ui.model.Conversation
import com.example.ecofruit.ui.data.constants.MessageStatus
import com.example.ecofruit.ui.data.mock.ChatMockData
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.isFromCurrentUser
import com.example.ecofruit.ui.screens.UserAvatar
import com.example.ecofruit.ui.viewmodels.ChatViewModel
import com.example.ecofruit.ui.viewmodels.ConversationUI
import com.example.ecofruit.ui.viewmodels.SettingsViewModel
import com.example.ecofruit.ui.viewmodels.UserViewModel
import com.example.ecofruit.ui.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ecofruit.R
import com.example.ecofruit.ui.components.NetworkStatusNotification
import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.screens.displayName
import com.example.ecofruit.ui.viewmodels.AuthViewModel

class ChatActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels { ViewModelFactory() }
    private val authViewModel : AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conversationId = intent.getStringExtra("conversation_id") ?: ""

        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val user = authViewModel.currentAppUserModel
            val conversationUI by chatViewModel.conversation.collectAsState()
            val chatState by chatViewModel.chatState.collectAsState()
            val messages by chatViewModel.messages.collectAsState()
            val isConnected by settingsViewModel.isConnectionSatisfied.collectAsStateWithLifecycle()

            val context = LocalContext.current

            LaunchedEffect(Unit) {
                user?.id?.let { uid ->
                    chatViewModel.getConversationUIFromId(conversationId, uid)
                    chatViewModel.markConversationAsRead(conversationId, uid)
                    chatViewModel.getMessagesFromConversation(conversationId)
                }
            }

            EcoFruitTheme (darkTheme = settings.darkTheme) {
                user?.let { currentUser ->
                    ChatScreen (
                        currentUser = currentUser,
                        conversation = conversationUI,
                        messages = messages,
                        chatState = chatState,
                        onProfileClick = {userId ->
                            Intent(context, ViewProfileActivity::class.java).also {
                                it.putExtra("user_id", userId)
                                context.startActivity(it)
                            }
                        },
                        onSend = { message ->
                            chatViewModel.addMessage(message)
                        },
                        onBackClick = { finish() },
                        isConnected = isConnected
                    )
                }
            }
        }
    }
}
// ── Screen ─────────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(
    currentUser: User = ChatMockData.currentUser,
    conversation: ConversationUI? = ConversationUI(base = ChatMockData.conversations.first(), otherUser = ChatMockData.marta),
    messages: List<ChatMessage> = ChatMockData.firstMessages,
    chatState: RequestUiState<Boolean>,
    onProfileClick: (String) -> Unit = {},
    onSend: (ChatMessage) -> Unit = {},
    onBackClick: () -> Unit = {},
    isConnected: Boolean = true
) {
    var messageText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    when(chatState) {
        is RequestUiState.Loading -> isLoading = true
        is RequestUiState.Success -> {
            isLoading = false
        }
        is RequestUiState.Error -> {
            isLoading = false
        }
        else -> Unit
    }
    if (conversation != null) {
        val usersById: Map<String, User> = remember {
            mapOf(
                currentUser.id to currentUser,
                conversation.otherUser.id to conversation.otherUser
            )
        }

        // Auto-scroll when new messages arrive
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }

        Scaffold(
            topBar = {
                ChatTopBar(
                    conversation = conversation,
                    currentUserId = currentUser.id,
                    onProfileClick = onProfileClick,
                    onBackClick = onBackClick,
                )
            },
            bottomBar = {
                ChatInputBar(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            val message = ChatMessage(
                                id = "local_${System.currentTimeMillis()}",
                                conversationId = conversation.base.id,
                                senderId = currentUser.id,
                                text = messageText.trim(),
                                timestamp = System.currentTimeMillis(),
                                status = MessageStatus.SENDING,
                            )
                            onSend(message)
                            messageText = ""
                            // No need to scroll here, LaunchedEffect(messages.size) handles it
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item { DateDivider(label = stringResource(R.string.chat_today)) }

                items(messages, key = { it.id }) { message ->
                    val isMe = message.isFromCurrentUser(currentUser.id)
                    val sender = usersById[message.senderId]

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
                    ) {
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = isMe,
                            sender = sender,
                        )
                    }
                }

                item{ NetworkStatusNotification(isConnected = isConnected) }

            }
        }
    }

}

// ── Top bar ────────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(
    conversation: ConversationUI,
    currentUserId: String,
    onProfileClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    val other = conversation.otherUser
    Surface(
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Reutilizamos UserAvatar de ConversationsScreen
            UserAvatar(user = conversation.otherUser, size = 40)

            Column(
                modifier = Modifier.weight(1f).clickable {
                    onProfileClick(conversation.otherUser.id)
                }
            ) {
                Text(
                    text = other.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Rating del interlocutor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("⭐", fontSize = 10.sp)
                    Text(
                        text = "%.1f".format(other.rating) + " · ${other.reviewCount} " + stringResource(R.string.chat_reviews),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Producto + tag
            Column(horizontalAlignment = Alignment.End) {
                val tag = conversation.base.conversationTag[currentUserId] ?: ConversationTag.CONSULTA
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = tag.displayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${conversation.base.productEmoji} ${conversation.base.productName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Message bubble ─────────────────────────────────────────────────────────

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    sender: User?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isFromCurrentUser) {
            UserAvatar(user = sender, size = 28)
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isFromCurrentUser) 18.dp else 4.dp,
                    bottomEnd = if (isFromCurrentUser) 4.dp else 18.dp,
                ),
                color = if (isFromCurrentUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                shadowElevation = if (isFromCurrentUser) 0.dp else 1.dp,
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isFromCurrentUser) {
                    MessageStatusIcon(status = message.status)
                }
            }
        }

        if (isFromCurrentUser) Spacer(Modifier.width(34.dp))
    }
}

// ── Message status icon ────────────────────────────────────────────────────

@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    val (icon, tint) = when (status) {
        MessageStatus.SENDING   -> Icons.Outlined.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.SENT      -> Icons.Outlined.Check to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.DELIVERED -> Icons.Outlined.DoneAll to MaterialTheme.colorScheme.onSurfaceVariant
        MessageStatus.READ      -> Icons.Outlined.DoneAll to MaterialTheme.colorScheme.primary
        MessageStatus.FAILED    -> Icons.Outlined.Schedule to MaterialTheme.colorScheme.error
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = Modifier.size(12.dp),
        tint = tint,
    )
}

// ── Date divider ───────────────────────────────────────────────────────────

@Composable
private fun DateDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
}

// ── Input bar ──────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        stringResource(R.string.chat_write_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name= "Light")
@Composable
private fun LightPreview() {
    EcoFruitTheme (darkTheme = false) {
        ChatScreen(
            chatState = RequestUiState.Idle(),
            isConnected = false
        )
    }
}

@Preview(showBackground = true, name= "Dark")
@Composable
private fun DarkPreview() {
    EcoFruitTheme (darkTheme = true) {
        ChatScreen(
            chatState = RequestUiState.Idle()
        )
    }
}
