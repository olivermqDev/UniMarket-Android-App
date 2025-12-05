// Archivo: app/src/main/java/com/atom/unimarket/presentation/screens/ProductsScreen.kt
package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.atom.unimarket.presentation.data.Product
import com.atom.unimarket.presentation.navigation.AppScreen
import com.atom.unimarket.presentation.products.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    modifier: Modifier = Modifier
) {
    val productState by productViewModel.productState.collectAsState()
    // 1. Obtenemos el estado del carrito para el contador
    val cartState by productViewModel.cartState.collectAsState()

    // 2. Cargamos el carrito al abrir la pantalla para que el número sea real
    LaunchedEffect(Unit) {
        productViewModel.getCartContents()
    }

    val categories = listOf("Todas", "Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // --- BARRA DE BÚSQUEDA + BOTÓN CARRITO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre barra y botón
        ) {
            // Barra de Búsqueda (Usa weight(1f) para ocupar el espacio disponible)
            OutlinedTextField(
                value = productState.searchQuery,
                onValueChange = { query -> productViewModel.onSearchQueryChange(query) },
                modifier = Modifier
                    .weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                ),
                trailingIcon = {
                    if (productState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { productViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                }
            )

            // Botón del Carrito con Contador (Badge)
            BadgedBox(
                badge = {
                    if (cartState.cartProducts.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary, // Color neón para el badge
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = cartState.cartProducts.size.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = { navController.navigate("cart_screen") },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Ver Carrito"
                    )
                }
            }
        }

        // --- FILTROS DE CATEGORÍAS ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                val isSelected = (productState.selectedCategory ?: "Todas") == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newCategory = if (category == "Todas") null else category
                        productViewModel.onCategorySelected(newCategory)
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.secondary,
                        borderWidth = 1.dp
                    ),
                    label = { Text(category) }
                )
            }
        }

        // --- LISTA DE PRODUCTOS ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (productState.isLoading && productState.products.isEmpty()) {
                CircularProgressIndicator()
            } else if (productState.error != null) {
                Text(text = "Error: ${productState.error}", textAlign = TextAlign.Center)
            } else if (productState.products.isEmpty()) {
                val message = when {
                    productState.searchQuery.isNotEmpty() -> "No se encontraron productos para \"${productState.searchQuery}\""
                    productState.selectedCategory != null -> "No hay productos en la categoría \"${productState.selectedCategory}\""
                    else -> "No hay productos disponibles. ¡Sé el primero!"
                }
                Text(text = message, textAlign = TextAlign.Center)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(productState.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = productState.favoriteProductIds.contains(product.id),
                            onFavoriteClick = {
                                productViewModel.toggleFavorite(product.id)
                            },
                            onClick = {
                                navController.navigate("${AppScreen.ProductDetail.route}/${product.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Box {
            Column {
                SubcomposeAsyncImage(
                    model = product.imageUrls.firstOrNull(),
                    loading = { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) },
                    error = { Icon(Icons.Default.BrokenImage, "Error", tint = MaterialTheme.colorScheme.error) },
                    contentDescription = product.name,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = product.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "S/ ${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onFavoriteClick() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Marcar como favorito",
                    tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else Color.LightGray
                )
            }
        }
    }
}