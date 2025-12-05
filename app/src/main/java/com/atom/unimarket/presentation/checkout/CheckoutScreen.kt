package com.atom.unimarket.presentation.checkout

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.paymentSuccess) {
        if (state.paymentSuccess) {
            Toast.makeText(context, "¡Pago reportado! Pendiente de verificación.", Toast.LENGTH_LONG).show()
            viewModel.resetPaymentSuccess()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                    val inputState = state.paymentInputs[group.sellerId] ?: PaymentInputState()
                    SellerPaymentSection(
                        group = group,
                        inputState = inputState,
                        currencyFormat = currencyFormat,
                        onYapeCodeChange = { code ->
                            viewModel.onYapeCodeChange(group.sellerId, code)
                        },
                        onReportPayment = {
                            viewModel.reportarPagoVendedor(group.sellerId)
                        },
                        onCopyText = { text, label ->
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SellerPaymentSection(
    group: OrderGroup,
    inputState: PaymentInputState,
    currencyFormat: NumberFormat,
    onYapeCodeChange: (String) -> Unit,
    onReportPayment: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Seller Info
            Text(
                text = "Vendedor: ${group.sellerName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Products List (Simplified)
            group.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        currencyFormat.format(item.price * item.quantity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Payment Details Section
            Text("Detalles de Pago (Yape)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // Yape Number Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Número Yape", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(group.sellerPhone.ifBlank { "No disponible" }, style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = { onCopyText(group.sellerPhone, "Número Yape") }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Número")
                }
            }

            // Total Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Monto Total", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        currencyFormat.format(group.subtotal),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF742284), // Yape Color
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { onCopyText(group.subtotal.toString(), "Monto Total") }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Monto")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Yape Payment Button
            Button(
                onClick = { 
                    onCopyText(group.sellerPhone, "Número Yape")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF742284) // Yape purple color
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("COPIAR NÚMERO Y PAGAR CON YAPE", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Después de realizar el pago, ingresa el código de operación:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input Section
            OutlinedTextField(
                value = inputState.yapeCode,
                onValueChange = onYapeCodeChange,
                label = { Text("Código de Operación Yape") },
                placeholder = { Text("Ej: 123456") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = onReportPayment,
                modifier = Modifier.fillMaxWidth(),
                enabled = inputState.yapeCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00CFA3), // Success/Action Color
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("REPORTAR PAGO")
            }
        }
    }
}
