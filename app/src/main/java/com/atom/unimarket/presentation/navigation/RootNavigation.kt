package com.atom.unimarket.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.atom.unimarket.presentation.chatbot.ChatbotViewModel
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.atom.unimarket.presentation.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavigation() {
    val mainNavController = rememberNavController()
    val productViewModel: ProductViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val chatbotViewModel: ChatbotViewModel = viewModel()

    NavHost(
        navController = mainNavController,
        // --- CORREGIDO: Usamos AppScreen para la ruta inicial ---
        startDestination = AppScreen.Login.route
    ) {
        // --- CORREGIDO: Todas las rutas usan AppScreen ---
        composable(route = AppScreen.Login.route) {
            LoginScreen(navController = mainNavController)
        }
        composable(route = AppScreen.SignUp.route) {
            SignUpScreen(navController = mainNavController)
        }

        composable(route = "main_screen") { // Esta es una ruta especial para el contenedor, está bien como string
            MainScreen(
                mainNavController = mainNavController,
                productViewModel = productViewModel,
                chatViewModel = chatViewModel
            )
        }

        // --- RUTA CLAVE CORREGIDA ---
        composable(
            // La ruta se construye usando el objeto AppScreen y el argumento
            route = "${AppScreen.ProductDetail.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductDetailScreen(
                productId = productId,
                productViewModel = productViewModel,
                chatViewModel = chatViewModel,
                navController = mainNavController
            )
        }

        // --- RUTA CLAVE CORREGIDA ---
        composable(
            route = "${AppScreen.Chat.route}/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            ChatScreen(
                navController = mainNavController,
                chatViewModel = chatViewModel,
                chatId = chatId
            )
        }

        composable(route = AppScreen.AddProduct.route) {
            AddProductScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )
        }

        composable(route = AppScreen.Chatbot.route) {
            ChatbotScreen(
                onNavigateBack = { mainNavController.navigateUp() }
            )
        }

        // Placeholders para Favoritos y Carrito (usando strings simples por ahora)
        // --- NUEVO: Registrar las pantallas de Favoritos y Carrito ---
        // --- NUEVO: Registrar las pantallas de Favoritos y Carrito ---
        composable(route = "favorites_screen") {
            // Llamada al Composable real que acabamos de crear
            FavoritesScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )}


        composable(route = "cart_screen") {
            CartScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )
        }
    }
}
