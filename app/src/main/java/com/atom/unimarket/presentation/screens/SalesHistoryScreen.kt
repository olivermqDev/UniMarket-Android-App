package com.atom.unimarket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.CartItem
import com.atom.unimarket.presentation.sales.SalesViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    navController: NavController,
    viewModel: SalesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Ventas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.orders.isEmpty()) {
                EmptySalesView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.orders) { order ->
                        SaleOrderCard(order, navController)
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SaleOrderCard(
    order: Order,
    navController: NavController
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = order.createdAt?.let { dateFormat.format(it) } ?: "Fecha desconocida"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Fecha y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateString, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Surface(
                    color = Color(0xFFE8F5E9), // Verde claro
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Información del Comprador con botón de Chat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Datos del Comprador", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                
                // Chat Button
                OutlinedButton(
                    onClick = {
                        // Navigate to chat with buyer
                        order.buyerId?.let { buyerId ->
                            navController.navigate("chat/$buyerId")
                        }
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(order.buyerName, fontWeight = FontWeight.Bold)
                    if (!order.buyerPhone.isNullOrBlank()) {
                        Text("Tel: ${order.buyerPhone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de Entrega
            Text("Tipo de Entrega", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    val deliveryType = order.deliveryType ?: "No especificado"
                    Text(
                        text = when (deliveryType.lowercase()) {
                            "delivery" -> "Envío a domicilio"
                            "pickup" -> "Recojo en campus"
                            else -> deliveryType
                        },
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Mostrar dirección o punto de entrega según el tipo
                    when (deliveryType.lowercase()) {
                        "delivery" -> {
                            val addr = order.shippingAddress
                            if (addr != null) {
                                Text(addr.street, style = MaterialTheme.typography.bodyMedium)
                                Text("${addr.city} - ${addr.zipCode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                if (!addr.phoneNumber.isNullOrBlank()) {
                                    Text("Tel: ${addr.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            } else {
                                Text("Dirección no especificada", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF6F00))
                            }
                        }
                        "pickup" -> {
                            val pickupPoint = order.pickupPoint ?: "Punto de recojo no especificado"
                            Text(pickupPoint, style = MaterialTheme.typography.bodyMedium)
                        }
                        else -> {
                            Text("Información de entrega no disponible", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Lista de Productos Vendidos
            Text("Productos (${order.items.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.forEach { item ->
                    SoldProductItem(item)
                }
            }

            // Total Ganado en esta orden
            val myTotal = order.items.sumOf { it.price * (it.quantity ?: 1) }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Total Venta: ", fontWeight = FontWeight.Bold)
                Text("S/ $myTotal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun SoldProductItem(item: CartItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row {
                Text("Cant: ${item.quantity ?: 1}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("S/ ${item.price}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptySalesView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aún no tienes ventas", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        Text("Tus productos vendidos aparecerán aquí", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}