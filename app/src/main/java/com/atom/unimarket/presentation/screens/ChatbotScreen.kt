package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atom.unimarket.presentation.chatbot.ChatbotViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onNavigateBack: () -> Unit,
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val messages by chatbotViewModel.messages.collectAsState()
    val isLoading by chatbotViewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Colores modernos y vibrantes
    val gradientStart = MaterialTheme.colorScheme.primary
    val gradientEnd = MaterialTheme.colorScheme.tertiary
    val onlineGreen = Color(0xFF4CAF50)
    val botMessageColor = MaterialTheme.colorScheme.surfaceVariant
    val userMessageColor = MaterialTheme.colorScheme.primaryContainer

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Avatar del bot con gradiente
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(gradientStart, gradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = "Bot",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Indicador de en línea
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(onlineGreen)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Asistente UniMarket",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "En línea • Responde al instante",
                            style = MaterialTheme.typography.labelSmall,
                            color = onlineGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Messages Area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser)
                            Arrangement.End else Arrangement.Start
                    ) {
                        if (!message.isUser) {
                            // Avatar del bot en mensajes
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(top = 2.dp, end = 12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(gradientStart, gradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Burbuja de mensaje
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .shadow(
                                    2.dp,
                                    RoundedCornerShape(
                                        topStart = if (message.isUser) 20.dp else 8.dp,
                                        topEnd = if (message.isUser) 8.dp else 20.dp,
                                        bottomStart = 20.dp,
                                        bottomEnd = 20.dp
                                    )
                                ),
                            shape = RoundedCornerShape(
                                topStart = if (message.isUser) 20.dp else 8.dp,
                                topEnd = if (message.isUser) 8.dp else 20.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isUser) userMessageColor else botMessageColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (message.isUser)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(top = 2.dp, end = 12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(gradientStart, gradientEnd)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .widthIn(max = 100.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = botMessageColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(3) { index ->
                                        LaunchedEffect(index) {
                                            delay(index * 200L)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(gradientStart, gradientEnd)
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Area moderna integrada
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Campo de texto con botón integrado
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp, max = 120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Campo de texto
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                placeholder = {
                                    Text(
                                        "Escribe tu mensaje...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                maxLines = 4,
                                singleLine = false
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Botón de enviar integrado
                            Card(
                                onClick = {
                                    if (messageText.isNotBlank() && !isLoading) {
                                        chatbotViewModel.sendMessage(messageText)
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (messageText.isNotBlank() && !isLoading) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (messageText.isNotBlank() && !isLoading) 4.dp else 0.dp
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Enviar",
                                        tint = if (messageText.isNotBlank() && !isLoading)
                                            Color.White
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Indicador de estado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(onlineGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Conectado • Listo para ayudarte",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}