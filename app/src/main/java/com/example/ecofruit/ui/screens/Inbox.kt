package com.example.ecofruit.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ecofruit.ui.data.constants.ConversationTag
import com.example.ecofruit.ui.data.mock.ChatMockData
import com.example.ecofruit.ui.data.model.RequestUiState
import com.example.ecofruit.ui.data.model.User
import com.example.ecofruit.ui.data.model.isFromCurrentUser
import com.example.ecofruit.ui.model.Conversation
import com.example.ecofruit.ui.theme.EcoFruitTheme
import com.example.ecofruit.ui.viewmodels.ChatViewModel
import com.example.ecofruit.ui.viewmodels.ConversationUI
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ecofruit.R

// ── Screen ─────────────────────────────────────────────────────────────────
private val TAG = "InboxScreen"

@Composable
fun InboxScreen(
    currentUser: User?,
    chatViewModel: ChatViewModel,
    onConversationClick: (Conversation) -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<ConversationTag?>(null) }
    
    val conversations by chatViewModel.conversations.collectAsState()
    val conversationState by chatViewModel.conversationsState.collectAsState()
    
    val isLoading = conversationState is RequestUiState.Loading
    val currentUserId = currentUser?.id ?: ""

    val filtered = conversations.filter { conv ->
        val matchesSearch = searchQuery.isBlank() ||
                conv.otherUser.name.contains(searchQuery, ignoreCase = true) ||
                conv.base.productName.contains(searchQuery, ignoreCase = true) ||
                conv.base.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true
        val tag = conv.base.conversationTag[currentUserId]
        val matchesTag = selectedFilter == null || tag == selectedFilter
        matchesSearch && matchesTag
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.inbox_messages),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val totalUnread = conversations.sumOf { it.base.unreadCount[currentUserId] ?: 0 }
                    if (totalUnread > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(
                                text = totalUnread.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.inbox_search),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem(
                        label = stringResource(R.string.inbox_all),
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                    )
                    ConversationTag.entries.forEach { tag ->
                        FilterChipItem(
                            label = tag.displayName(),
                            selected = selectedFilter == tag,
                            onClick = { selectedFilter = if (selectedFilter == tag) null else tag },
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        if (isLoading && conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.inbox_no_conversations),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(filtered, key = { _, c -> c.base.id }) { index, conversation ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    ) {
                        ConversationItem(
                            conversation = conversation,
                            currentUserId = currentUserId,
                            onClick = { onConversationClick(conversation.base) },
                        )
                    }
                    if (index < filtered.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

// ── Conversation item ──────────────────────────────────────────────────────

@Composable
private fun ConversationItem(
    currentUserId: String,
    conversation: ConversationUI,
    onClick: () -> Unit,
) {
    val other = conversation.otherUser
    val lastMsg = conversation.base.lastMessage
    val isMine = lastMsg?.isFromCurrentUser(currentUserId) == true
    val unreadCount = conversation.base.unreadCount[currentUserId] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        UserAvatar(user = other, size = 50)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = other.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = lastMsg?.timestamp?.toRelativeLabel() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unreadCount > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(conversation.base.productEmoji, fontSize = 11.sp)
                        Text(
                            text = conversation.base.productName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            if (isMine) append(stringResource(R.string.inbox_you) + " ")
                            append(lastMsg?.text ?: "")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unreadCount > 0)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    )
                }

                if (unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Shared avatar composable ───────────────────────────────────────────────

@Composable
fun UserAvatar(user: User?, size: Int) {
    val sizeDp = size.dp
    Box(
        modifier = Modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(Color(user?.avatarColor() ?: 0xFF888888L)),
        contentAlignment = Alignment.Center,
    ) {
        if (user != null && user.hasAvatar()) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = user?.avatarInitials() ?: "?",
                style = if (size >= 40)
                    MaterialTheme.typography.labelLarge
                else
                    MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = (size / 3.5).sp,
            )
        }
    }
}

// ── Filter chip ────────────────────────────────────────────────────────────

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(10.dp),
    )
}

// ── Helpers ────────────────────────────────────────────────────────────────

@Composable
fun ConversationTag.displayName() = when (this) {
    ConversationTag.COMPRA   -> stringResource(R.string.inbox_buy)
    ConversationTag.VENTA    -> stringResource(R.string.inbox_sell)
    ConversationTag.CONSULTA -> stringResource(R.string.inbox_consultation)
}

private fun Long.toRelativeLabel(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> {
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConversationsScreenPreview() {
    EcoFruitTheme { 
        // Solo para preview
        InboxScreen(
            currentUser = ChatMockData.currentUser,
            chatViewModel = viewModel(), 
        )
    }
}
