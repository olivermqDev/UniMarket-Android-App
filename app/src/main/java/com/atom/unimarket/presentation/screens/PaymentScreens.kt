package com.atom.unimarket.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.atom.unimarket.presentation.card.CardViewModel
import com.atom.unimarket.presentation.products.ProductViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

// Constantes
object PaymentMethods {
    const val YAPE = "YAPE"
    const val CARD = "CARD"
    const val SAVED_CARD = "SAVED_CARD"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    navController: NavController,
    viewModel: ProductViewModel = koinViewModel()
) {
    val cartState by viewModel.cartState.collectAsState()
    var selectedMethod by remember { mutableStateOf(PaymentMethods.SAVED_CARD) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    // Aseguramos que el carrito esté cargado al entrar aquí
    LaunchedEffect(Unit) {
        viewModel.getCartContents()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pagar Pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Monto Total a Pagar", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = currencyFormat.format(cartState.totalPrice),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text("Método de Pago", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                PaymentOptionItem(
                    title = "Mis Tarjetas Guardadas",
                    subtitle = "Seleccionar una tarjeta existente",
                    icon = Icons.Default.Wallet,
                    isSelected = selectedMethod == PaymentMethods.SAVED_CARD,
                    color = Color(0xFF00897B),
                    onClick = { selectedMethod = PaymentMethods.SAVED_CARD }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentOptionItem(
                    title = "Nueva Tarjeta",
                    subtitle = "Crédito o Débito",
                    icon = Icons.Default.CreditCard,
                    isSelected = selectedMethod == PaymentMethods.CARD,
                    color = Color(0xFF1A237E),
                    onClick = { selectedMethod = PaymentMethods.CARD }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentOptionItem(
                    title = "Yape",
                    subtitle = "Pago Instantáneo Digital",
                    icon = Icons.Default.Smartphone,
                    isSelected = selectedMethod == PaymentMethods.YAPE,
                    color = Color(0xFF742284),
                    onClick = { selectedMethod = PaymentMethods.YAPE }
                )
            }

            Button(
                onClick = {
                    when (selectedMethod) {
                        PaymentMethods.SAVED_CARD -> navController.navigate("saved_cards_screen")
                        PaymentMethods.CARD -> navController.navigate("card_payment_screen")
                        PaymentMethods.YAPE -> navController.navigate("yape_payment_screen")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "CONTINUAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PaymentOptionItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(color), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YapePaymentScreen(
    navController: NavController,
    viewModel: ProductViewModel = koinViewModel()
) {
    val cartState by viewModel.cartState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    // 1. Recargamos el carrito al entrar
    LaunchedEffect(Unit) {
        viewModel.getCartContents()
    }

    // 2. Manejo de Errores
    LaunchedEffect(cartState.error) {
        cartState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(cartState.checkoutSuccess) {
        if (cartState.checkoutSuccess) {
            Toast.makeText(context, "¡Pago verificado! Orden creada.", Toast.LENGTH_LONG).show()
            viewModel.resetCheckoutState()
            navController.navigate("main_screen") { popUpTo("main_screen") { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Finalizar Pago") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Monto Total", style = MaterialTheme.typography.bodyLarge)
                Text(currencyFormat.format(cartState.totalPrice), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color(0xFF742284))
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.size(250.dp).background(Color.White, RoundedCornerShape(16.dp)).border(4.dp, Brush.linearGradient(listOf(Color(0xFF00CFA3), Color(0xFF742284))), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Smartphone, contentDescription = "QR Yape", modifier = Modifier.size(100.dp), tint = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Escanea o Abre la App Yape", fontWeight = FontWeight.Bold)
            }
            Button(onClick = { viewModel.checkout(PaymentMethods.YAPE) }, modifier = Modifier.fillMaxWidth(), enabled = !cartState.isLoading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CFA3))) {
                if (cartState.isLoading) CircularProgressIndicator(color = Color.White) else Text("YA PAGUÉ (CONFIRMAR)")
            }
        }
    }
}

// --- PANTALLA NUEVA TARJETA CORREGIDA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentScreen(
    navController: NavController,
    productViewModel: ProductViewModel = koinViewModel(),
    cardViewModel: CardViewModel = koinViewModel()
) {
    val cartState by productViewModel.cartState.collectAsState()
    val context = LocalContext.current

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var saveCardChecked by remember { mutableStateOf(false) }

    // 1. IMPORTANTE: Recargamos el carrito al entrar a esta pantalla
    // Esto es necesario porque si el ViewModel es nuevo, el carrito estaría vacío y el checkout fallaría silenciosamente.
    LaunchedEffect(Unit) {
        productViewModel.getCartContents()
    }

    // 2. Manejo de Errores (Si falla el checkout, muestra por qué)
    LaunchedEffect(cartState.error) {
        cartState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(cartState.checkoutSuccess) {
        if (cartState.checkoutSuccess) {
            Toast.makeText(context, "¡Pago exitoso!", Toast.LENGTH_LONG).show()
            if (saveCardChecked) {
                cardViewModel.saveCard(cardNumber, cardHolder, expiryDate)
            }
            productViewModel.resetCheckoutState()
            navController.navigate("main_screen") { popUpTo("main_screen") { inclusive = true } }
        }
    }

    fun formatCardForDisplay(text: String) = text.chunked(4).joinToString(" ")
    fun formatDateForDisplay(text: String) = if (text.length > 2) "${text.substring(0, 2)}/${text.substring(2)}" else text

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Pago con Tarjeta") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Tarjeta Visual
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Text("Tarjeta de Crédito", color = Color.White, modifier = Modifier.align(Alignment.TopStart))
                        Text(
                            text = if(cardNumber.isNotEmpty()) formatCardForDisplay(cardNumber) else "**** **** **** ****",
                            color = Color.White, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.Center)
                        )
                        Text(
                            text = if(cardHolder.isNotEmpty()) cardHolder.uppercase() else "NOMBRE TITULAR",
                            color = Color.White, modifier = Modifier.align(Alignment.BottomStart)
                        )
                        Text(
                            text = if(expiryDate.isNotEmpty()) formatDateForDisplay(expiryDate) else "MM/AA",
                            color = Color.White, modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                }

                // Campos de Texto
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        if (cleaned.length <= 16) cardNumber = cleaned
                    },
                    label = { Text("Número de Tarjeta") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    visualTransformation = CreditCardNumberTransformation()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() }
                            if (cleaned.length <= 4) expiryDate = cleaned
                        },
                        label = { Text("MM/AA") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        visualTransformation = ExpiryDateTransformation()
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() }
                            if (cleaned.length <= 4) cvv = cleaned
                        },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("Nombre del Titular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth().clickable { saveCardChecked = !saveCardChecked }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = saveCardChecked, onCheckedChange = { saveCardChecked = it })
                    Text("Guardar tarjeta para futuras compras")
                }
            }

            Button(
                onClick = {
                    // Validación
                    if (cardNumber.length != 16) {
                        Toast.makeText(context, "El número de tarjeta debe tener 16 dígitos", Toast.LENGTH_SHORT).show()
                    } else if (expiryDate.length != 4) {
                        Toast.makeText(context, "Fecha inválida (MM/AA)", Toast.LENGTH_SHORT).show()
                    } else if (cvv.length < 3) {
                        Toast.makeText(context, "CVV inválido", Toast.LENGTH_SHORT).show()
                    } else if (cardHolder.isEmpty()) {
                        Toast.makeText(context, "Ingresa el nombre del titular", Toast.LENGTH_SHORT).show()
                    } else {
                        // Todo correcto, intentamos pagar
                        productViewModel.checkout(PaymentMethods.CARD)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !cartState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (cartState.isLoading) CircularProgressIndicator(color = Color.White) else Text("PAGAR AHORA")
            }
        }
    }
}

// Transformaciones visuales (Sin cambios)
class CreditCardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText = creditCardFilter(text)
}
fun creditCardFilter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
    var out = ""
    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i % 4 == 3 && i != 15) out += " "
    }
    val creditCardOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 7) return offset + 1
            if (offset <= 11) return offset + 2
            if (offset <= 16) return offset + 3
            return 19
        }
        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 9) return offset - 1
            if (offset <= 14) return offset - 2
            if (offset <= 19) return offset - 3
            return 16
        }
    }
    return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
}
class ExpiryDateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += "/"
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}