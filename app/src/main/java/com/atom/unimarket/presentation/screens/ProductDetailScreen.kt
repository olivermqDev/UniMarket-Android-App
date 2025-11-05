package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.atom.unimarket.presentation.chat.ChatViewModel
import com.atom.unimarket.presentation.navigation.AppScreen
import com.atom.unimarket.presentation.products.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String?,
    productViewModel: ProductViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController
) {
    val state by productViewModel.productState.collectAsState()

    // --- ELIMINADO: Ya no necesitamos un estado local para el favorito ---
    // var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = productId) {
        if (productId != null) {
            productViewModel.getProductById(productId)
            // Ya no es necesario llamar a una función checkIfFavorite,
            // porque el listener del ViewModel se encarga de todo.
        }
    }

    val product = state.products.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product?.name ?: "Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                // --- INICIO DE CAMBIOS ---
                actions = {
                    if (product != null) { // Solo muestra el botón si el producto ha cargado
                        IconToggleButton(
                            // 1. Leemos el estado directamente del ViewModel
                            checked = state.favoriteProductIds.contains(product.id),
                            // 2. Llamamos a la función del ViewModel al cambiar
                            onCheckedChange = {
                                productViewModel.toggleFavorite(product.id)
                            }
                        ) {
                            Icon(
                                // 3. La lógica de qué ícono mostrar es la misma
                                imageVector = if (state.favoriteProductIds.contains(product.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Marcar como favorito",
                                tint = if (state.favoriteProductIds.contains(product.id)) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                // --- FIN DE CAMBIOS ---
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
            } else if (product != null) {
                // ... (El resto del código del Body y los botones inferiores no cambia)
                Box(modifier = Modifier.fillMaxSize()) {
                    // Contenido que se puede scrollear
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            // Dejamos espacio abajo para los botones
                            .padding(bottom = 80.dp)
                    ) {
                        SubcomposeAsyncImage(
                            model = product.imageUrls.firstOrNull(),
                            loading = { /* ... */ },
                            contentDescription = product.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(text = "S/ ${"%.2f".format(product.price)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Vendido por: ${product.sellerName}", style = MaterialTheme.typography.bodyMedium)
                            Divider()
                            Text(text = "Descripción", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = product.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                        }
                    }

                    // Fila de botones de acción fijos en la parte inferior
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Chat
                        OutlinedButton(onClick = {
                            chatViewModel.findOrCreateChat(product) { chatId ->
                                navController.navigate("${AppScreen.Chat.route}/$chatId")
                            }
                        },
                            modifier = Modifier.weight(1f),
                            enabled = product.sellerUid != productViewModel.getCurrentUserId()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat")
                        }

                        // Botón de Añadir al Carrito
                        Button(
                            onClick = {
                                // --- CAMBIO: Llamamos a la nueva función del ViewModel ---
                                if (product != null) {
                                    productViewModel.addToCart(product.id)
                                    // Opcional: Mostrar un Toast o Snackbar de confirmación
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Añadir al Carrito", modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Añadir al Carrito")
                        }
                    }
                }
            } else {
                Text(text = "Producto no disponible.")
            }
        }
    }
}
