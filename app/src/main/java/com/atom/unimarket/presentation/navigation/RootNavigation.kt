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
// --- INICIO DE CAMBIOS ---
// import androidx.lifecycle.viewmodel.compose.viewModel // <-- 1. ESTE SE VA
import org.koin.androidx.compose.koinViewModel // <-- 2. AÑADIMOS ESTE
// --- FIN DE CAMBIOS ---
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.atom.unimarket.presentation.address.AddressViewModel
import com.atom.unimarket.presentation.chatbot.ChatbotViewModel
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.dashboard.DashboardViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.atom.unimarket.presentation.screens.*
import com.atom.unimarket.screens.CartScreen
import com.atom.unimarket.screens.MyAddressScreen
import com.atom.unimarket.screens.AddAddressScreen
import com.atom.unimarket.screens.CardPaymentScreen
import com.atom.unimarket.screens.PaymentMethodScreen
import com.atom.unimarket.screens.SalesHistoryScreen
import com.atom.unimarket.screens.SavedCardsScreen
import com.atom.unimarket.screens.SelectAddressScreen
import com.atom.unimarket.screens.YapePaymentScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavigation() {
    val mainNavController = rememberNavController()
    // --- INICIO DE CAMBIOS ---
    val productViewModel: ProductViewModel = koinViewModel() // <-- 3. CAMBIADO
    val chatViewModel: ChatViewModel = koinViewModel() // <-- 3. CAMBIADO
    val chatbotViewModel: ChatbotViewModel = koinViewModel() // <-- 3. CAMBIADO
    val dashboardViewModel : DashboardViewModel = koinViewModel() // <-- 3. CAMBIADO
    val addressViewModel : AddressViewModel = koinViewModel()  // <-- 3. NUEVO

    // --- FIN DE CAMBIOS ---

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
                chatbotViewModel = chatbotViewModel, // <-- Este está bien
                onNavigateBack = { // <-- Este es el parámetro que faltaba
                    mainNavController.popBackStack() // <-- Aquí pones la lógica de navegación
                }
            )
        }
        //Se agrego el Dashboard
        composable(BottomBarScreen.Dashboard.route) {
            DashboardScreen(
                navController = mainNavController,
                dashboardViewModel = dashboardViewModel
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
                viewModel = productViewModel
            )
        }
        // --- NUEVO : Añadida ruta a MyProductsScreen
        composable(route = "my_products_screen") {
            MyProductsScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )
        }
        // --- NUEVO : Añadida ruta a MyAddressScreen
        composable(route = "my_address_screen") {
            MyAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel
            )
        }

        // --- NUEVO : Añadida ruta a Agregar Dirección
        composable(route = "add_address_screen") {
            AddAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel
            )
        }
        //--- NUEVO : Añadida ruta a Seleccionar Direccion
        composable("select_address_screen") {
            SelectAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel // Usamos la misma instancia compartida
            )
        }


        // ---NUEVO : Añadida rutas de Metodo de pago
        // Pantalla de Seleccionar Metodo de pago
        composable("payment_method_screen") {
            PaymentMethodScreen(navController = mainNavController, viewModel = productViewModel)
        }

        // Pantalla de Yape
        composable("yape_payment_screen") {
            YapePaymentScreen(navController = mainNavController, viewModel = productViewModel)
        }

        // Pantalla de Tarjeta
        composable("card_payment_screen") {
            CardPaymentScreen(navController = mainNavController)
        }
        // Pantalla de Seleccionar Tarjeta
        composable("saved_cards_screen") {
            SavedCardsScreen(navController = mainNavController)
        }
        //--- NUEVO: Añadido Pantalla de Historial de Ventas
        composable("sales_history_screen") {
            SalesHistoryScreen(navController = mainNavController)
        }

    }
}