package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.orders.OrderHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderHistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // ESTADO DEL FILTRO:
    // false = Solo pedidos activos (pendientes/confirmados)
    // true = Ver historial completo (incluye terminados)
    var showAllOrders by remember { mutableStateOf(false) }

    // LÓGICA DE FILTRADO
    val filteredOrders = remember(state.orders, showAllOrders) {
        if (showAllOrders) {
            state.orders // Mostrar todo
        } else {
            // Ocultar los que ya terminaron (Entregados, Rechazados, Cancelados)
            state.orders.filter { order ->
                order.status != "DELIVERED" &&
                        order.status != "pago_rechazado" &&
                        order.status != "CANCELLED"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Pedidos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón en la barra superior para alternar el filtro
                    IconToggleButton(
                        checked = showAllOrders,
                        onCheckedChange = { showAllOrders = it }
                    ) {
                        Icon(
                            imageVector = if (showAllOrders) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = if (showAllOrders) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra informativa del filtro (Opcional pero útil para UX)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showAllOrders) "Mostrando: Historial Completo" else "Mostrando: Pedidos Activos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón tipo Chip más explícito
                    FilterChip(
                        selected = showAllOrders,
                        onClick = { showAllOrders = !showAllOrders },
                        label = { Text(if (showAllOrders) "Ocultar Terminados" else "Ver Terminados") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (showAllOrders) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // CONTENIDO DE LA LISTA
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.error != null) {
                    ErrorMessageView(state.error!!)
                } else if (filteredOrders.isEmpty()) {
                    if (state.orders.isNotEmpty() && !showAllOrders) {
                        EmptyActiveOrdersView { showAllOrders = true }
                    } else {
                        EmptyOrdersView()
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { order ->
                            OrderItem(
                                order = order,
                                onVerifyOrder = { viewModel.verifyOrder(order.id) } // Conectamos el botón al ViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

// Vista cuando no hay pedidos ACTIVOS, pero sí historial
@Composable
fun EmptyActiveOrdersView(onShowHistory: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes pedidos pendientes", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onShowHistory) {
            Text("Ver historial de pedidos terminados")
        }
    }
}

@Composable
fun EmptyOrdersView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No has realizado pedidos aún", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun OrderItem(
    order: Order,
    onVerifyOrder: () -> Unit // Nuevo parámetro para el callback
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${order.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OrderStatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fecha: ${order.createdAt?.let { dateFormat.format(it) } ?: "Pendiente"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))
            val itemCount = order.items.sumOf { it.quantity }
            Text(text = "$itemCount productos", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(
                    text = currencyFormat.format(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // BOTÓN DE VERIFICACIÓN
            // Solo se muestra si el pedido está en estado "PENDING_VERIFICATION"
            if (order.status == "PENDING_VERIFICATION") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onVerifyOrder,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C), // Verde confirmación
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificar Pedido (Confirmar Entrega)")
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String) {
    val (color, text) = when (status) {
        "PENDING_VERIFICATION" -> Color(0xFFFFA000) to "Verificando"
        "CONFIRMED", "pago_confirmado", "COMPLETED" -> Color(0xFF388E3C) to "Confirmado"
        "DELIVERED" -> Color.Blue to "Entregado"
        "pago_rechazado", "CANCELLED" -> Color.Red to "Rechazado"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorMessageView(errorMsg: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
    }
}