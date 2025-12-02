package com.atom.unimarket.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atom.unimarket.domain.model.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
    orderId: String
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Find the specific order from the list (assuming list is loaded)
    // Ideally we should fetch specific order details, but for now we use the list
    val orderWithSeller = (uiState as? HistoryUiState.Success)?.orders?.find { it.order.idPedido == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Pedido") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (orderWithSeller == null) {
                if (uiState is HistoryUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Text("Pedido no encontrado", modifier = Modifier.align(Alignment.Center))
                }
            } else {
                val order = orderWithSeller.order
                val status = OrderStatus.fromString(order.estado)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Status Header
                    if (status == OrderStatus.PagoRechazado) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Pago Rechazado", color = Color.Red, fontWeight = FontWeight.Bold)
                                    Text("Por favor, verifica tu comprobante.", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Text("Vendedor: ${orderWithSeller.sellerName}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total: S/ ${String.format("%.2f", order.total)}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Productos:", fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(order.productos) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.quantity}x ${item.name}", modifier = Modifier.weight(1f))
                                Text("S/ ${String.format("%.2f", item.price * item.quantity)}")
                            }
                            HorizontalDivider()
                        }
                    }

                    if (status == OrderStatus.PagoRechazado) {
                        Button(
                            onClick = { /* TODO: Implement correction flow */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Corregir Pago (Próximamente)")
                        }
                    }
                }
            }
        }
    }
}
