package com.atom.unimarket.presentation.navigation

// app/src/main/java/com/atom/unimarket/presentation/navigation/AppScreen.kt
sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object SignUp : AppScreen("signup_screen")
    object Products : AppScreen("products_screen")
    object Profile : AppScreen("profile_screen") // <-- AÑADE ESTA LÍNEA
    object AddProduct : AppScreen("add_product_screen") // <-- Y ESTA
    object ProductDetail : AppScreen("product_detail")
    object Chat : AppScreen("chat")
    object Conversations : AppScreen("conversations")
    object Chatbot : AppScreen("chatbot_screen")
}
    