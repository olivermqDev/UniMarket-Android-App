package com.atom.unimarket.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.checkout.CheckoutViewModel
import com.atom.unimarket.presentation.checkout.OrderGroup
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.paymentSuccess) {
        if (state.paymentSuccess) {
            Toast.makeText(context, "¡Pedido realizado! Pendiente de verificación.", Toast.LENGTH_LONG).show()
            viewModel.resetPaymentSuccess()
            // Si no quedan grupos, volver al inicio o carrito
            if (state.orderGroups.isEmpty()) {
                navController.navigate("main_screen") { popUpTo("main_screen") { inclusive = true } }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Checkout por Vendedor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.orderGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay pedidos pendientes.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.orderGroups) { group ->
                    SellerOrderCard(
                        group = group,
                        currencyFormat = currencyFormat,
                        onPayClick = {
                            // 1. Copiar número
                            clipboardManager.setText(AnnotatedString(group.sellerPhone))
                            Toast.makeText(context, "Número ${group.sellerPhone} copiado", Toast.LENGTH_SHORT).show()
                            // 2. Abrir Yape
                            viewModel.payToSeller(context, group.sellerPhone)
                        },
                        onConfirmClick = {
                            viewModel.confirmPayment(group)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    group: OrderGroup,
    currencyFormat: NumberFormat,
    onPayClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vendedor: ${group.sellerName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lista de items
            group.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity}x ${item.name}", modifier = Modifier.weight(1f))
                    Text(currencyFormat.format(item.price * item.quantity), fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total a Pagar:", fontWeight = FontWeight.Bold)
                Text(
                    text = currencyFormat.format(group.subtotal),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF742284), // Yape color
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPayClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF742284))
            ) {
                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PAGAR CON YAPE (Copiar y Abrir)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00CFA3))
            ) {
                Text("YA REALICÉ EL PAGO (CONFIRMAR)")
            }
        }
    }
}