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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AddHome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material.icons.outlined.ShoppingCart
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
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.R
import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = koinViewModel()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreen.EditProfile.route) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Mis Favoritos") },
                leadingContent = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("favorites_screen")
                }
            )

            ListItem(
                headlineContent = { Text("Mis Pedidos") },
                leadingContent = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("order_history_screen")
                }
            )

            ListItem(
                headlineContent = { Text("Historial de Ventas") },
                leadingContent = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("sales_history_screen")
                }
            )
            
            ListItem(
                headlineContent = { Text("Administrar direcciones") },
                leadingContent = { Icon(Icons.Outlined.AddHome, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("my_address_screen")
                }
            )

            ListItem(
                headlineContent = { Text("Mis productos en venta") },
                leadingContent = { Icon(Icons.Outlined.Shop, contentDescription = null) },
                modifier = Modifier.clickable {
                    navController.navigate("my_products_screen")
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}