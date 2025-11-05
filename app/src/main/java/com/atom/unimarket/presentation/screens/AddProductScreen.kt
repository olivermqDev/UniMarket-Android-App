package com.atom.unimarket.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.atom.unimarket.presentation.products.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 1. ESTADO PARA EL MENÚ DE CATEGORÍAS
    val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
    var expandedCategoryMenu by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.last()) } // 'Otros' por defecto

    val productState by productViewModel.productState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    // LaunchedEffect no necesita cambios, tu implementación es correcta.
    LaunchedEffect(productState) {
        if (productState.uploadSuccess) {
            Toast.makeText(context, "Producto publicado con éxito", Toast.LENGTH_SHORT).show()
            productViewModel.resetProductState()
            navController.popBackStack()
        }
        productState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            productViewModel.resetProductState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
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
            Box(
                // ... (El Box de la imagen no necesita cambios)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = "Imagen del producto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = "Añadir foto", modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para añadir una foto")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Producto") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !productState.isLoading)
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. MENÚ DESPLEGABLE PARA CATEGORÍAS ---
            ExposedDropdownMenuBox(
                expanded = expandedCategoryMenu,
                onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(), // Importante para anclar el menú
                    readOnly = true,
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryMenu,
                    onDismissRequest = { expandedCategoryMenu = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategoryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(150.dp), enabled = !productState.isLoading)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = price, onValueChange = { newPrice -> if (newPrice.count { it == '.' } <= 1 && newPrice.all { it.isDigit() || it == '.' }) { price = newPrice } }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, leadingIcon = { Text("$") }, enabled = !productState.isLoading)
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val priceDouble = price.toDoubleOrNull()
                    if (name.isBlank() || description.isBlank() || priceDouble == null || priceDouble <= 0 || imageUri == null) {
                        Toast.makeText(context, "Por favor, completa todos los campos y añade una imagen.", Toast.LENGTH_SHORT).show()
                    } else {
                        // --- 3. LLAMADA CORREGIDA A addProduct ---
                        productViewModel.addProduct(
                            name = name,
                            description = description,
                            price = priceDouble,
                            category = selectedCategory, // Pasamos la categoría seleccionada
                            imageUri = imageUri // Pasamos el Uri de la imagen
                        ) { success, error ->
                            if (!success) {
                                println("Fallo al iniciar la subida: $error")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !productState.isLoading
            ) {
                if (productState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Publicar Producto")
                }
            }
        }
    }
}
