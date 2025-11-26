package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.data.Chat
import com.atom.unimarket.presentation.navigation.AppScreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    val state by chatViewModel.conversationsUiState.collectAsState()

    // Llama a la función para empezar a escuchar los chats cuando la pantalla se compone
    LaunchedEffect(Unit) {
        chatViewModel.listenForConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" Mis Conversaciones") },
                /*
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
                */
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = "Error: ${state.error}")
            } else if (state.chats.isEmpty()) {
                Text("No tienes conversaciones activas.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.chats, key = { it.id }) { chat ->
                        ConversationItem(
                            chat = chat,
                            onClick = {
                                navController.navigate("${AppScreen.Chat.route}/${chat.id}")
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del producto del chat
        SubcomposeAsyncImage(
            model = chat.productInvolved.productImageUrl,
            contentDescription = chat.productInvolved.productName,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { CircularProgressIndicator() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Nombre del producto
            Text(
                text = chat.productInvolved.productName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Último mensaje
            Text(
                text = chat.lastMessage.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Fecha del último mensaje
        chat.lastMessage.timestamp?.let {
            Text(
                text = formatTimestamp(it),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Función de utilidad para formatear la fecha
private fun formatTimestamp(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}
