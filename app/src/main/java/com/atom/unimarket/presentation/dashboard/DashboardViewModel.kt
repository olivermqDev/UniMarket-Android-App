package com.atom.unimarket.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val totalProducts: Int = 0,
    val averagePrice: Double = 0.0,
    val productsByCategory: Map<String, Int> = emptyMap(),
    val productsBySeller: Map<String, Int> = emptyMap()
)

class DashboardViewModel : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Ejemplo simulado — aquí podrías usar repositorios reales
            val categoryData = mapOf(
                "Electrónicos" to 35,
                "Ropa" to 22,
                "Libros" to 15,
                "Muebles" to 18
            )
            val sellerData = mapOf(
                "Juan" to 40,
                "Ana" to 28,
                "Carlos" to 15,
                "Diana" to 25
            )

            _dashboardState.value = DashboardState(
                totalProducts = categoryData.values.sum(),
                averagePrice = 120.5,
                productsByCategory = categoryData,
                productsBySeller = sellerData
            )
        }
    }
}
