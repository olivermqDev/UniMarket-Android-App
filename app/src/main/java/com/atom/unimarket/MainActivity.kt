package com.atom.unimarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.atom.unimarket.presentation.navigation.AppNavigation // Asegúrate de que los otros archivos ya existan
import com.atom.unimarket.ui.theme.UnimarketTheme
import com.atom.unimarket.presentation.navigation.RootNavigation // <-- IMPORTAR

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnimarketTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Esta línea reemplaza a "Greeting" y carga todo el sistema nuevo
                    RootNavigation()
                }
            }
        }
    }
}
