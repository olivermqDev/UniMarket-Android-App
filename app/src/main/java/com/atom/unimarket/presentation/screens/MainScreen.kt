// app/src/main/java/com/atom/unimarket/presentation/screens/MainScreen.kt
package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.atom.unimarket.R
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.navigation.AppNavigation
// import com.atom.unimarket.presentation.navigation.AppScreens // ELIMINADO: ESTO NO EXISTE
import com.atom.unimarket.presentation.navigation.BottomBarScreen
import com.atom.unimarket.presentation.products.ProductViewModel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SmartToy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    productViewModel: ProductViewModel,
    chatViewModel: ChatViewModel
) {
    val bottomBarNavController = rememberNavController()

    val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = { BottomBar(navController = bottomBarNavController) },

        floatingActionButton = {
            when (currentDestination?.route) {
                // CASO 1: Si la pantalla es "Inicio"
                BottomBarScreen.Home.route -> {
                    FloatingActionButton(
                        onClick = {
                            // CORREGIDO: Usamos el string de la ruta, como en RootNavigation.kt
                            mainNavController.navigate("add_product_screen")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir producto"
                        )
                    }
                }

                // CASO 2: Si la pantalla es "Conversaciones"
                BottomBarScreen.Conversations.route -> {
                    FloatingActionButton(
                        onClick = {
                            // CORREGIDO: Usamos el string de la ruta, como en RootNavigation.kt
                            mainNavController.navigate("chatbot_screen")
                        }
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = "Asistente de IA")
                    }
                }

                else -> {}
            }
        },
        floatingActionButtonPosition = FabPosition.End

    ) { paddingValues ->
        AppNavigation(
            modifier = Modifier.padding(paddingValues),
            mainNavController = mainNavController,
            bottomBarNavController = bottomBarNavController,
            productViewModel = productViewModel,
            chatViewModel = chatViewModel
        )
    }
}

// El resto del código (BottomBar y AddItem) no necesita cambios y está correcto.
@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Conversations,
        BottomBarScreen.Profile,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        label = { Text(text = screen.title) },
        icon = { Icon(imageVector = screen.icon, contentDescription = "Navigation Icon") },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}
