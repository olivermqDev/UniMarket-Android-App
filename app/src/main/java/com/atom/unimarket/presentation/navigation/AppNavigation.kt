package com.atom.unimarket.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.atom.unimarket.presentation.screens.ConversationsScreen
import com.atom.unimarket.presentation.screens.ProductsScreen
import com.atom.unimarket.presentation.screens.ProfileScreen
import com.atom.unimarket.presentation.screens.DashboardScreen
import com.atom.unimarket.presentation.dashboard.DashboardViewModel


@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    mainNavController: NavHostController,
    bottomBarNavController: NavHostController,
    productViewModel: ProductViewModel,
    chatViewModel: ChatViewModel,
    dashboardViewModel: DashboardViewModel

) {
    NavHost(
        navController = bottomBarNavController,
        startDestination = BottomBarScreen.Home.route,
        modifier = modifier
    ) {
        // --- CAMBIO CLAVE: Se elimina el "navigation" anidado ---
        // Ahora, la ruta de la pestaña "Inicio" es directamente la pantalla de productos.
        composable(route = BottomBarScreen.Home.route) {
            ProductsScreen(
                navController = mainNavController,
                productViewModel = productViewModel,
                // El modifier se hereda del NavHost, así que no es necesario pasarlo aquí
                // si el Column de ProductsScreen ya usa el modifier que recibe.
            )
        }

        // --- PANTALLA PARA LA PESTAÑA "CHATS" ---
        composable(route = BottomBarScreen.Conversations.route) {
            ConversationsScreen(
                navController = mainNavController,
                chatViewModel = chatViewModel
            )
        }

        // --- PANTALLA PARA LA PESTAÑA "PERFIL" ---
        composable(route = BottomBarScreen.Profile.route) {
            ProfileScreen(
                navController = mainNavController
            )
        }

        // --- PANTALLA PARA LA PESTAÑA "DASHBOARD" ---
        composable(BottomBarScreen.Dashboard.route) {
            DashboardScreen(
                navController = mainNavController,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}
