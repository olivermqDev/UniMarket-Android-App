package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.presentation.data.Product
import com.atom.unimarket.presentation.navigation.AppScreen
import com.atom.unimarket.presentation.products.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    val productState by productViewModel.productState.collectAsState()
    val currentUserId = productViewModel.getCurrentUserId()

    // Filtrar solo los productos del usuario actual
    val myProducts = productState.products.filter { it.sellerUid == currentUserId }

    // Cargar productos cuando la pantalla aparece
    LaunchedEffect(Unit) {
        productViewModel.loadUserProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Productos en Venta") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                productState.isLoading -> {
                    CircularProgressIndicator()
                }
                productState.error != null -> {
                    Text(
                        text = "Error: ${productState.error}",
                        textAlign = TextAlign.Center
                    )
                }
                myProducts.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📦\nAún no tienes productos en venta",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = {
                                navController.navigate("add_product_screen")
                            }
                        ) {
                            Text("➕ Agregar mi primer producto")
                        }
                    }
                }
                else -> {
                    // Mostrar grid de productos
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(myProducts, key = { it.id }) { product ->
                            MyProductCard(
                                product = product,
                                onClick = {
                                    navController.navigate("${AppScreen.ProductDetail.route}/${product.id}")
                                },
                                onEditClick = {
                                    // Navegar a editar producto (opcional)
                                    navController.navigate("edit_product_screen/${product.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Card personalizada para productos del usuario
@Composable
fun MyProductCard(
    product: Product,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen del producto (usando Coil)
            AsyncImage(
                model = product.imageUrls.firstOrNull(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )

            // Información del producto
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Estado del producto
                //ProductStatusChip(status = product.status ?: "activo")

                Spacer(modifier = Modifier.height(8.dp))

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Botón de editar (solo visible para el dueño)
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar producto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Chip para mostrar el estado del producto
@Composable
fun ProductStatusChip(status: String) {
    val (text, color) = when (status) {
        "activo" -> "🟢 Activo" to MaterialTheme.colorScheme.primary
        "vendido" -> "💰 Vendido" to MaterialTheme.colorScheme.secondary
        "en_negociacion" -> "🟡 En Negociación" to MaterialTheme.colorScheme.tertiary
        "reservado" -> "🟣 Reservado" to Color(0xFF9C27B0)
        else -> "⚪ $status" to MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}