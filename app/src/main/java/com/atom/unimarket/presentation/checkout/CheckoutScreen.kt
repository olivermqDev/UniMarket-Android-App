package com.atom.unimarket.presentation.checkout

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.atom.unimarket.domain.model.CartItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun CheckoutScreen(
    onOrderSuccess: () -> Unit,
    checkoutViewModel: CheckoutViewModel = koinViewModel()
) {
    val uiState by checkoutViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CheckoutUiState.Success -> {
                Toast.makeText(context, "Pedidos realizados con éxito", Toast.LENGTH_LONG).show()
                onOrderSuccess()
            }
            is CheckoutUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            // Add TopBar if needed
        },
        bottomBar = {
            if (uiState is CheckoutUiState.Content) {
                Button(
                    onClick = {
                        checkoutViewModel.confirmAllOrders()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirmar Todos los Pedidos", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is CheckoutUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CheckoutUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(state.sellerGroups) { sellerState ->
                            SellerOrderCard(
                                state = sellerState,
                                onPaymentInfoUpdated = { code, uri ->
                                    checkoutViewModel.updatePaymentInfo(sellerState.sellerId, code, uri)
                                },
                                onDeliveryMethodUpdated = { method ->
                                    checkoutViewModel.updateDeliveryMethod(sellerState.sellerId, method)
                                },
                                onPickupLocationUpdated = { location ->
                                    checkoutViewModel.updatePickupLocation(sellerState.sellerId, location)
                                },
                                onDeliveryAddressUpdated = { address ->
                                    checkoutViewModel.updateDeliveryAddress(sellerState.sellerId, address)
                                }
                            )
                        }
                    }
                }
                is CheckoutUiState.Error -> {
                    // Error is handled in LaunchedEffect, but we can show retry button here
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${(uiState as CheckoutUiState.Error).message}")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    state: SellerCheckoutState,
    onPaymentInfoUpdated: (String, Uri?) -> Unit,
    onDeliveryMethodUpdated: (String) -> Unit,
    onPickupLocationUpdated: (String) -> Unit,
    onDeliveryAddressUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onPaymentInfoUpdated(state.yapeCode, uri)
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pedido a: ${state.sellerName}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.quantity}x ${item.name}", fontSize = 14.sp)
                    Text("S/ ${item.price * item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Delivery Options Section
            Text("Método de Entrega", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.deliveryType == "personal",
                    onClick = { onDeliveryMethodUpdated("personal") }
                )
                Text("Entrega Personal en Campus", fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = state.deliveryType == "delivery",
                    onClick = { onDeliveryMethodUpdated("delivery") }
                )
                Text("Delivery", fontSize = 14.sp)
            }

            if (state.deliveryType == "personal") {
                OutlinedTextField(
                    value = state.pickupPoint,
                    onValueChange = onPickupLocationUpdated,
                    label = { Text("Punto de Encuentro (ej. Puerta 3)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = state.deliveryAddress,
                    onValueChange = onDeliveryAddressUpdated,
                    label = { Text("Dirección de Entrega") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", fontWeight = FontWeight.Bold)
                Text("S/ ${state.total}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pago con Yape", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val uri = if (state.sellerPhone.isNotBlank()) {
                            "yape://qr/${state.sellerPhone}" 
                        } else {
                            "yape://qr/"
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Yape no está instalado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF742284)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yapear", color = Color.White)
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(state.total.toString()))
                        Toast.makeText(context, "Monto copiado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copiar Monto")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.yapeCode,
                onValueChange = { onPaymentInfoUpdated(it, state.proofUri) },
                label = { Text("Código de Operación") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = if (state.proofUri != null) ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) else ButtonDefaults.buttonColors()
            ) {
                Text(if (state.proofUri != null) "Comprobante Adjuntado ✓" else "Subir Captura")
            }
        }
    }
}
