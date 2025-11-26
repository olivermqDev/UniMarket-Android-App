package com.atom.unimarket.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons // --- NUEVO ---
import androidx.compose.material.icons.outlined.FavoriteBorder // --- NUEVO ---
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material.icons.outlined.ShoppingCart // --- NUEVO ---
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.R
import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class) // --- NUEVO: Para usar ListItem ---
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    val context = LocalContext.current
    var displayName by remember(user?.displayName) { mutableStateOf(user?.displayName ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                authViewModel.uploadProfileImage(it) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al subir la foto: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Tu código para la foto de perfil, nombre, etc. no cambia)
        Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (!user?.photoUrl.isNullOrEmpty()) {
                AsyncImage(model = user?.photoUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Image(painter = painterResource(id = R.drawable.ic_person_placeholder), contentDescription = "Añadir foto de perfil", modifier = Modifier.size(70.dp))
            }
        }
        Text("Toca para cambiar foto", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Nombre de Usuario") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { authViewModel.updateDisplayName(displayName); Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar Nombre")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Email: ${user?.email ?: "Cargando..."}", style = MaterialTheme.typography.bodyLarge)

        // --- INICIO DE CAMBIOS: SECCIÓN DE FAVORITOS Y CARRITO ---
        Spacer(modifier = Modifier.height(32.dp))
        Divider()

        // Opción para ver Favoritos
        ListItem(
            headlineContent = { Text("Mis Favoritos") },
            leadingContent = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
            modifier = Modifier.clickable {
                // Navegamos a la nueva pantalla de favoritos
                navController.navigate("favorites_screen")
            }
        )

        // Opción para ver el Carrito
        ListItem(
            headlineContent = { Text("Mi Carrito de Compras") },
            leadingContent = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
            modifier = Modifier.clickable {
                // Navegaremos a la nueva pantalla del carrito
                navController.navigate("cart_screen")
            }
        )
        // ---NUEVO CAMBIO : Opcion para ver productos en venta
        ListItem(
            headlineContent = { Text("Mis productos en venta") },
            leadingContent = { Icon(Icons.Outlined.Shop, contentDescription = null) },
            modifier = Modifier.clickable {
                // Navegaremos a la nueva pantalla de productos en venta
                navController.navigate("my_products_screen")
            }
        )

        Divider()
        // --- FIN DE CAMBIOS ---

        Spacer(modifier = Modifier.weight(1f))

        Button(
            // En ProfileScreen.kt, en el botón de "Cerrar Sesión"
            onClick = {
                authViewModel.signOut()
                // --- CORREGIDO: Usar AppScreen para la ruta ---
                navController.navigate(AppScreen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            ,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}
