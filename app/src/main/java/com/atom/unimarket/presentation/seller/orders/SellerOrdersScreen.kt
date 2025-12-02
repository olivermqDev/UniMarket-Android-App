package com.atom.unimarket.presentation.seller.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atom.unimarket.domain.model.Order
import com.atom.unimarket.domain.model.OrderStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SellerOrdersScreen(
    viewModel: SellerOrdersViewModel = koinViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            // Add TopBar
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes pedidos aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderItem(order = order, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, viewModel: SellerOrdersViewModel) {
    val context = LocalContext.current
    val orderStatus = OrderStatus.fromString(order.estado)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Pedido #${order.idPedido.take(8)}", fontWeight = FontWeight.Bold)
            Text(text = "Estado: ${order.estado}", color = getStatusColor(orderStatus))
            Text(text = "Total: S/ ${order.total}")
            Text(text = "Fecha: ${formatDate(order.fechaCreado)}")
            Text(text = "Entrega: ${order.tipoEntrega}")
            if (order.tipoEntrega == "delivery") {
                Text(text = "Dirección: ${order.direccionEntrega}")
            } else {
                Text(text = "Punto: ${order.puntoEntrega}")
            }

            if (order.codigoYape.isNotEmpty()) {
                Text(text = "Cód. Yape: ${order.codigoYape}", fontWeight = FontWeight.Bold)
            }

            if (order.urlComprobante.isNotEmpty()) {
                Text(
                    text = "Ver Comprobante",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(order.urlComprobante))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (orderStatus) {
                OrderStatus.PagoReportado -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.approveOrder(order.idPedido) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Aprobar")
                        }
                        Button(
                            onClick = { viewModel.rejectOrder(order.idPedido) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Rechazar")
                        }
                    }
                }
                OrderStatus.PagoConfirmado -> {
                    Button(onClick = { viewModel.updateStatus(order.idPedido, OrderStatus.EnPreparacion) }) {
                        Text("Marcar en Preparación")
                    }
                }
                OrderStatus.EnPreparacion -> {
                    Button(onClick = { 
                        val nextStatus = if (order.tipoEntrega == "delivery") OrderStatus.EnCamino else OrderStatus.Entregado
                        viewModel.updateStatus(order.idPedido, nextStatus) 
                    }) {
                        Text(if (order.tipoEntrega == "delivery") "Enviar (En Camino)" else "Entregar")
                    }
                }
                OrderStatus.EnCamino -> {
                    Button(onClick = { viewModel.updateStatus(order.idPedido, OrderStatus.Entregado) }) {
                        Text("Marcar Entregado")
                    }
                }
                else -> {
                    // No actions for other states (e.g., Entregado, PagoRechazado)
                }
            }
        }
    }
}

fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.PagoReportado -> Color.Blue
        OrderStatus.PagoConfirmado -> Color(0xFF4CAF50) // Green
        OrderStatus.PagoRechazado -> Color.Red
        OrderStatus.Entregado -> Color.Gray
        else -> Color.Black
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
