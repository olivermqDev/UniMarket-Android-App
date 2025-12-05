package com.atom.unimarket.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavController
import com.atom.unimarket.presentation.auth.AuthViewModel
import com.atom.unimarket.presentation.navigation.AppScreen
import com.atom.unimarket.R

@Composable
fun LoginScreen(
    navController: NavController,
    // --- INICIO DE CAMBIOS ---
    authViewModel: AuthViewModel = koinViewModel() // <-- 3. CAMBIADO
    // --- FIN DE CAMBIOS ---
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo_proyecto),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text("UniMarket", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar Sesión")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(AppScreen.SignUp.route) }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }

    // Este bloque se ejecuta cuando 'authState' cambia
    LaunchedEffect(authState) {
        // Comprobamos si el objeto 'user' NO es nulo
        if (authState.user != null) {
            Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            // --- ÚNICO CAMBIO: NAVEGAR A LA PANTALLA PRINCIPAL CONTENEDORA ---
            navController.navigate("main_screen") {
                // Limpia el stack para que el usuario no pueda volver atrás a la pantalla de login
                popUpTo(AppScreen.Login.route) { inclusive = true }
            }
        }
        // Mostramos el error si existe
        authState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
        }
    }
}