// app/src/main/java/com/atom/unimarket/presentation/navigation/BottomBarScreen.kt
package com.atom.unimarket.presentation.navigation
import com.atom.unimarket.R

// Cambiamos ImageVector por Int (ID de drawable)
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val iconResId: Int // Aquí guardamos el drawable
) {
    object Home : BottomBarScreen(
        route = "PRODUCTS_GRAPH",
        title = "Inicio",
        iconResId = R.drawable.home // drawable
    )

    object Conversations : BottomBarScreen(
        route = AppScreen.Conversations.route,
        title = "Chats",
        iconResId = R.drawable.chat // reemplaza con tu drawable
    )

    object Profile : BottomBarScreen(
        route = AppScreen.Profile.route,
        title = "Perfil",
        iconResId = R.drawable.perfil // reemplaza con tu drawable
    )

    object Dashboard : BottomBarScreen(
        route = "DASHBOARD_SCREEN",
        title = "Dashboard",
        iconResId = R.drawable.dashboard // reemplaza con tu drawable
    )
}
