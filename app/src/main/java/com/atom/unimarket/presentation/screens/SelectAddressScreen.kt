package com.atom.unimarket.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.data.Address
import com.atom.unimarket.presentation.address.AddressViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAddressScreen(
    navController: NavController,
    viewModel: AddressViewModel = koinViewModel(),
    productViewModel: ProductViewModel = koinViewModel() // <-- Inyectamos también este VM
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAddresses()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seleccionar Dirección", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("add_address_screen") }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Dirección")
                    }
                }
            )
        },
        bottomBar = {
            if (state.addresses.isNotEmpty()) {
                Button(
                    onClick = {
                        if (state.selectedAddressId != null) {
                            // --- PASO CLAVE: Guardamos la dirección en ProductViewModel ---
                            val selectedAddress = viewModel.getSelectedAddress()
                            if (selectedAddress != null) {
                                productViewModel.setShippingAddress(selectedAddress)
                                navController.navigate("payment_method_screen")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    enabled = state.selectedAddressId != null,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar al Pago", fontSize = 16.sp)
                }
            }
        }
    ) { paddingValues ->
        // ... (El resto del UI se mantiene idéntico al anterior) ...
        // Simplemente copiamos el contenido del Box para completitud del archivo
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.addresses.isEmpty()) {
                NoAddressesView(onAddClick = { navController.navigate("add_address_screen") })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "¿Dónde quieres recibir tu pedido?",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(state.addresses) { address ->
                        SelectableAddressItem(
                            address = address,
                            isSelected = address.id == state.selectedAddressId,
                            onSelect = { viewModel.selectAddress(address.id) }
                        )
                    }
                }
            }
        }
    }
}
// ... (Funciones SelectableAddressItem y NoAddressesView se mantienen igual) ...
@Composable
fun SelectableAddressItem(address: Address, isSelected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val borderStroke = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke,
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = address.alias, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(text = address.street, style = MaterialTheme.typography.bodyMedium)
                Text(text = "${address.city} - ${address.zipCode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, modifier = Modifier.size(24.dp))
        }
    }
}
@Composable
fun NoAddressesView(onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes direcciones guardadas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth().height(50.dp).padding(top=24.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Agregar Nueva Dirección")
        }
    }
}