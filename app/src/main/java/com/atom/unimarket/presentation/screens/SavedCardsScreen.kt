package com.atom.unimarket.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.data.SavedCard
import com.atom.unimarket.presentation.card.CardViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import com.atom.unimarket.presentation.products.PaymentMethod
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCardsScreen(
    navController: NavController,
    cardViewModel: CardViewModel = koinViewModel(),
    productViewModel: ProductViewModel = koinViewModel()
) {
    val state by cardViewModel.state.collectAsState()
    val cartState by productViewModel.cartState.collectAsState()
    val context = LocalContext.current

    // --- CORRECCIÓN CLAVE: Cargar el carrito al entrar ---
    // Esto asegura que productViewModel tenga los productos listos para el checkout
    LaunchedEffect(Unit) {
        productViewModel.getCartContents()
        // Opcional: Recargar tarjetas para asegurar frescura
        cardViewModel.loadCards()
    }

    // Escuchar el éxito de la compra
    LaunchedEffect(cartState.checkoutSuccess) {
        if (cartState.checkoutSuccess) {
            Toast.makeText(context, "¡Pago con tarjeta guardada exitoso!", Toast.LENGTH_LONG).show()
            productViewModel.resetCheckoutState()
            navController.navigate("main_screen") {
                popUpTo("main_screen") { inclusive = true }
            }
        }
    }

    // Manejo de errores
    LaunchedEffect(cartState.error) {
        cartState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Tarjetas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.cards.isEmpty()) {
                // Si no hay tarjetas, sugerimos agregar una nueva
                EmptyCardsView(onAddNew = { navController.navigate("card_payment_screen") })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text("Selecciona una tarjeta para pagar", style = MaterialTheme.typography.titleMedium)
                    }
                    items(state.cards) { card ->
                        SavedCardItem(
                            card = card,
                            onSelect = {
                                // Pagar directamente con la tarjeta seleccionada
                                // Usamos el método CARD genérico, ya que la info de pago ya está guardada en el usuario
                                productViewModel.processPayment(PaymentMethod.CARD)
                            },
                            onDelete = {
                                cardViewModel.deleteCard(card.id)
                            }
                        )
                    }
                }
            }
            
            // Show loading indicator if checkout is in progress
            if (cartState.isLoading) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SavedCardItem(card: SavedCard, onSelect: () -> Unit, onDelete: () -> Unit) {
    // Fondo degradado simulando una tarjeta real
    val gradient = Brush.horizontalGradient(
        if (card.brand == "VISA") listOf(Color(0xFF1A237E), Color(0xFF3949AB))
        else listOf(Color(0xFFB71C1C), Color(0xFFE53935))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient).padding(20.dp)) {
            // Marca
            Text(
                text = card.brand,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // Chip simulado
            Icon(
                Icons.Default.CreditCard,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.align(Alignment.TopStart).size(40.dp)
            )

            // Número enmascarado
            Text(
                text = "**** **** **** ${card.last4}",
                color = Color.White,
                fontSize = 22.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Titular y Fecha
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TITULAR", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text(card.cardHolder.uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPIRA", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text(card.expiryDate, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Botón eliminar pequeño
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.White)
            }
        }
    }
}

@Composable
fun EmptyCardsView(onAddNew: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes tarjetas guardadas", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        // Button removed as card payment is disabled
    }
}