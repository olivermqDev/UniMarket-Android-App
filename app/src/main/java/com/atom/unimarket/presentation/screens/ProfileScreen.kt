package com.atom.unimarket.presentation.screens

import android.Manifest
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
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.R
import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.navigation.AppScreen
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    val context = LocalContext.current
    var displayName by remember(user?.displayName) { mutableStateOf(user?.displayName ?: "") }

    // 🔹 Estado para mostrar el diálogo
    var showPermissionDialog by remember { mutableStateOf(false) }

    // 🔹 Lanzador de selección de imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                authViewModel.uploadProfileImage(it) { success, error ->
                    Toast.makeText(
                        context,
                        if (success) "Foto de perfil actualizada" else "Error al subir: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    // 🔹 Lanzador para permiso (dinámico según versión)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // 🔹 Detectar y solicitar permiso correcto
    fun handleImageSelection() {

        // Permiso según versión
        val requiredPermission =
            if (android.os.Build.VERSION.SDK_INT >= 33)
                Manifest.permission.READ_MEDIA_IMAGES // Android 13+
            else
                Manifest.permission.READ_EXTERNAL_STORAGE // Android 12-

        val granted = ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            // Si ya tenía permiso
            imagePickerLauncher.launch("image/*")
        } else {
            // Mostrar diálogo personalizado primero
            showPermissionDialog = true
        }
    }

    // 🔹 DIÁLOGO PERSONALIZADO
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso necesario") },
            text = {
                Text(
                    "UniMarket necesita acceder a tu galería para seleccionar una foto de perfil. " +
                            "Tus fotos no serán usadas para ningún otro propósito."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false

                    val requiredPermission =
                        if (android.os.Build.VERSION.SDK_INT >= 33)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE

                    permissionLauncher.launch(requiredPermission)
                }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // -------------------- UI COMPLETA --------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // FOTO DE PERFIL
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { handleImageSelection() },
            contentAlignment = Alignment.Center
        ) {
            if (!user?.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_person_placeholder),
                    contentDescription = "Añadir foto de perfil",
                    modifier = Modifier.size(70.dp)
                )
            }
        }

        Text("Toca para cambiar foto", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(32.dp))

        // NOMBRE DE USUARIO
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre de Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                authViewModel.updateDisplayName(displayName)
                Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Nombre")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Email: ${user?.email ?: "Cargando..."}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))
        Divider()

        // FAVORITOS
        ListItem(
            headlineContent = { Text("Mis Favoritos") },
            leadingContent = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
            modifier = Modifier.clickable { navController.navigate("favorites_screen") }
        )

        // CARRITO
        ListItem(
            headlineContent = { Text("Mi Carrito de Compras") },
            leadingContent = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
            modifier = Modifier.clickable { navController.navigate("cart_screen") }
        )

        Divider()

        Spacer(modifier = Modifier.weight(1f))

        // CERRAR SESIÓN
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


