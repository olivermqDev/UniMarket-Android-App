package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.navigation.AppScreen
import com.atom.unimarket.presentation.navigation.BottomBarScreen
import com.atom.unimarket.presentation.products.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    val productState by productViewModel.productState.collectAsState()

    // Usamos LaunchedEffect para llamar a la función del ViewModel una sola vez cuando la pantalla aparece
    LaunchedEffect(Unit) {
        productViewModel.getFavoriteProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos") },
                navigationIcon = {
                    IconButton(onClick = {  navController.popBackStack()  }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
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
            if (productState.isLoading) {
                CircularProgressIndicator()
            } else if (productState.error != null) {
                Text(text = "Error: ${productState.error}", textAlign = TextAlign.Center)
            } else if (productState.products.isEmpty()) {
                Text(text = "Aún no tienes productos favoritos.\n¡Añade algunos!", textAlign = TextAlign.Center)
            } else {
                // La misma grilla que usamos en ProductsScreen
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
