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
import org.koin.androidx.compose.koinViewModel
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
import com.atom.unimarket.presentation.screens.PaymentMethodScreen
import com.atom.unimarket.screens.SalesHistoryScreen
import com.atom.unimarket.screens.SelectAddressScreen
import com.atom.unimarket.presentation.checkout.CheckoutViewModel
import com.atom.unimarket.presentation.checkout.CheckoutScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavigation() {
    val mainNavController = rememberNavController()
    val productViewModel: ProductViewModel = koinViewModel()
    val chatViewModel: ChatViewModel = koinViewModel()
    val chatbotViewModel: ChatbotViewModel = koinViewModel()
    val dashboardViewModel : DashboardViewModel = koinViewModel()
    val addressViewModel : AddressViewModel = koinViewModel()
    val checkoutViewModel: CheckoutViewModel = koinViewModel()

    NavHost(
        navController = mainNavController,
        startDestination = AppScreen.Login.route
    ) {
        composable(route = AppScreen.Login.route) {
            LoginScreen(navController = mainNavController)
        }
        composable(route = AppScreen.SignUp.route) {
            SignUpScreen(navController = mainNavController)
        }

        composable(route = "main_screen") {
            MainScreen(
                mainNavController = mainNavController,
                productViewModel = productViewModel,
                chatViewModel = chatViewModel
            )
        }

        composable(
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
                chatbotViewModel = chatbotViewModel,
                onNavigateBack = {
                    mainNavController.popBackStack()
                }
            )
        }
        
        composable(BottomBarScreen.Dashboard.route) {
            DashboardScreen(
                navController = mainNavController,
                dashboardViewModel = dashboardViewModel
            )
        }

        composable(route = "favorites_screen") {
            FavoritesScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )
        }

        composable(route = "cart_screen") {
            CartScreen(
                navController = mainNavController,
                viewModel = productViewModel
            )
        }
        
        composable(route = "my_products_screen") {
            MyProductsScreen(
                navController = mainNavController,
                productViewModel = productViewModel
            )
        }

        composable(route = "my_address_screen") {
            MyAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel
            )
        }

        composable(route = "add_address_screen") {
            AddAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel
            )
        }

        composable("select_address_screen") {
            SelectAddressScreen(
                navController = mainNavController,
                viewModel = addressViewModel,
                productViewModel = productViewModel
            )
        }
        composable("sales_history_screen") {
            SalesHistoryScreen(navController = mainNavController)
        }
        
        composable("order_history_screen") {
            OrderHistoryScreen(navController = mainNavController)
        }

        composable(route = "checkout_screen") {
            CheckoutScreen(
                navController = mainNavController,
                viewModel = checkoutViewModel
            )
        }

        composable(route = "payment_method_screen") {
            PaymentMethodScreen(
                navController = mainNavController,
                viewModel = checkoutViewModel
            )
        }

        composable(route = AppScreen.EditProfile.route) {
            EditProfileScreen(navController = mainNavController)
        }
    }
}