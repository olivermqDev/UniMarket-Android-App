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
    val categories = listOf("Todas", "Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // ... (Barra de búsqueda y chips de categorías no cambian)
        OutlinedTextField(
            value = productState.searchQuery,
            onValueChange = { query -> productViewModel.onSearchQueryChange(query) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            label = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            trailingIcon = {
                if (productState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { productViewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            }
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = (productState.selectedCategory ?: "Todas") == category,
                    onClick = {
                        val newCategory = if (category == "Todas") null else category
                        productViewModel.onCategorySelected(newCategory)
                    },
                    label = { Text(category) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
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
                        // --- INICIO DE CAMBIOS ---
                        ProductCard(
                            product = product,
                            // 1. Leemos el estado de favorito desde el ViewModel
                            isFavorite = productState.favoriteProductIds.contains(product.id),
                            // 2. Llamamos a la función del ViewModel al hacer clic
                            onFavoriteClick = {
                                productViewModel.toggleFavorite(product.id)
                            },
                            onClick = {
                                navController.navigate("${AppScreen.ProductDetail.route}/${product.id}")
                            }
                        )
                        // --- FIN DE CAMBIOS ---
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Box {
            Column {
                SubcomposeAsyncImage(
                    model = product.imageUrls.firstOrNull(),
                    loading = { CircularProgressIndicator() },
                    error = { Icon(Icons.Default.BrokenImage, "Error") },
                    contentDescription = product.name,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                    // Puedes mostrar el precio formateado si lo prefieres
                    Text(text = "S/ ${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodyLarge)
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
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }
    }
}
