// app/src/main/java/com/atom/unimarket/presentation/navigation/BottomBarScreen.kt
package com.atom.unimarket.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = "PRODUCTS_GRAPH", // Un nombre para el grafo anidado
        title = "Inicio",
        icon = Icons.Default.Home
    )

    object Conversations : BottomBarScreen(
        route = AppScreen.Conversations.route,
        title = "Chats",
        icon = Icons.Default.Chat
    )

    object Profile : BottomBarScreen(
        route = AppScreen.Profile.route,
        title = "Perfil",
        icon = Icons.Default.Person
    )
}
