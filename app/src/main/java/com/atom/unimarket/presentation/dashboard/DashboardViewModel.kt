package com.atom.unimarket.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class DashboardViewModel : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // LiveData para todos los gráficos
    private val _salesColumnData = MutableLiveData<List<Pair<String, Float>>>()
    val salesColumnData: LiveData<List<Pair<String, Float>>> = _salesColumnData

    private val _categoryData = MutableLiveData<List<Pair<String, Float>>>()
    val categoryData: LiveData<List<Pair<String, Float>>> = _categoryData

    private val _combinedChartData = MutableLiveData<List<Pair<String, Float>>>()
    val combinedChartData: LiveData<List<Pair<String, Float>>> = _combinedChartData

    private val _productStatusData = MutableLiveData<List<Pair<String, Float>>>()
    val productStatusData: LiveData<List<Pair<String, Float>>> = _productStatusData

    private val _favoritesData = MutableLiveData<List<Pair<String, Int>>>()
    val favoritesData: LiveData<List<Pair<String, Int>>> = _favoritesData

    private val _userFlowData = MutableLiveData<List<Pair<String, Float>>>()
    val userFlowData: LiveData<List<Pair<String, Float>>> = _userFlowData

    private val _dailyActivityData = MutableLiveData<List<Pair<String, Float>>>()
    val dailyActivityData: LiveData<List<Pair<String, Float>>> = _dailyActivityData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDashboardData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Cargar todos los datos
                launch { loadSalesColumnData() }.join()
                launch { loadCategoryData() }.join()
                launch { loadCombinedChartData() }.join()
                launch { loadProductStatusData() }.join()
                launch { loadFavoritesData() }.join()
                launch { loadUserFlowData() }.join()
                launch { loadDailyActivityData() }.join()

            } catch (e: Exception) {
                // En caso de error, cargar datos mock
                loadMockData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 1. Ventas Mensuales
    private suspend fun loadSalesColumnData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("products")
                .whereEqualTo("sellerUid", currentUserId)
                .whereEqualTo("status", "vendido")
                .get()
                .await()

            val salesByMonth = mutableMapOf<String, Float>()
            val calendar = Calendar.getInstance()

            snapshot.documents.forEach { document ->
                val timestamp = document.getTimestamp("createdAt")
                val price = document.getDouble("price")?.toFloat() ?: 0f

                if (timestamp != null) {
                    calendar.time = timestamp.toDate()
                    val month = getMonthName(calendar.get(Calendar.MONTH))
                    salesByMonth[month] = salesByMonth.getOrDefault(month, 0f) + price
                }
            }

            val orderedMonths = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val result = orderedMonths.map { month ->
                month to salesByMonth.getOrDefault(month, 0f)
            }
            _salesColumnData.postValue(result)
            */

            // DATOS FALSOS
            val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val sales = listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)
            _salesColumnData.postValue(months.zip(sales))

        } catch (e: Exception) {
            // Fallback a datos falsos
            val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val sales = listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)
            _salesColumnData.postValue(months.zip(sales))
        }
    }

    // 2. Ventas por Categoría
    private suspend fun loadCategoryData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("products")
                .whereEqualTo("sellerUid", currentUserId)
                .whereEqualTo("status", "vendido")
                .get()
                .await()

            val categoriesRevenue = mutableMapOf<String, Float>()

            snapshot.documents.forEach { document ->
                val category = document.getString("category") ?: "Otros"
                val price = document.getDouble("price")?.toFloat() ?: 0f
                categoriesRevenue[category] = categoriesRevenue.getOrDefault(category, 0f) + price
            }

            val predefinedCategories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val result = predefinedCategories.map { category ->
                category to categoriesRevenue.getOrDefault(category, 0f)
            }
            _categoryData.postValue(result)
            */

            // DATOS FALSOS
            val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val revenues = listOf(4500f, 3200f, 1800f, 1200f, 800f, 600f)
            _categoryData.postValue(categories.zip(revenues))

        } catch (e: Exception) {
            val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val revenues = listOf(4500f, 3200f, 1800f, 1200f, 800f, 600f)
            _categoryData.postValue(categories.zip(revenues))
        }
    }

    // 3. Ventas vs Promedio Móvil
    private suspend fun loadCombinedChartData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("products")
                .whereEqualTo("sellerUid", currentUserId)
                .whereEqualTo("status", "vendido")
                .get()
                .await()

            val monthlySales = mutableMapOf<String, Float>()
            val calendar = Calendar.getInstance()

            snapshot.documents.forEach { document ->
                val timestamp = document.getTimestamp("createdAt")
                val price = document.getDouble("price")?.toFloat() ?: 0f

                if (timestamp != null) {
                    calendar.time = timestamp.toDate()
                    val month = getMonthName(calendar.get(Calendar.MONTH))
                    monthlySales[month] = monthlySales.getOrDefault(month, 0f) + price
                }
            }

            val orderedMonths = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val salesData = orderedMonths.map { month ->
                monthlySales.getOrDefault(month, 0f)
            }
            val result = orderedMonths.zip(salesData)
            _combinedChartData.postValue(result)
            */

            // DATOS FALSOS
            val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val sales = listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)
            _combinedChartData.postValue(months.zip(sales))

        } catch (e: Exception) {
            val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
            val sales = listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)
            _combinedChartData.postValue(months.zip(sales))
        }
    }

    // 4. Estado de Productos
    private suspend fun loadProductStatusData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("products")
                .whereEqualTo("sellerUid", currentUserId)
                .get()
                .await()

            var activeCount = 0f
            var soldCount = 0f
            var negotiationCount = 0f
            var reservedCount = 0f

            snapshot.documents.forEach { document ->
                when (document.getString("status")) {
                    "activo" -> activeCount++
                    "vendido" -> soldCount++
                    "en_negociacion" -> negotiationCount++
                    "reservado" -> reservedCount++
                }
            }

            val statusList = mutableListOf<Pair<String, Float>>()
            statusList.add("Activos" to activeCount)
            statusList.add("Vendidos" to soldCount)

            if (negotiationCount > 0) statusList.add("En Negociación" to negotiationCount)
            if (reservedCount > 0) statusList.add("Reservados" to reservedCount)

            _productStatusData.postValue(statusList)
            */

            // DATOS FALSOS
            _productStatusData.postValue(listOf(
                "Activos" to 8f,
                "Vendidos" to 12f,
                "En Negociación" to 3f,
                "Reservados" to 2f
            ))

        } catch (e: Exception) {
            _productStatusData.postValue(listOf(
                "Activos" to 8f, "Vendidos" to 12f, "En Negociación" to 3f
            ))
        }
    }

    // 5. Tus Favoritos
    private suspend fun loadFavoritesData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("users")
                .document(currentUserId!!)
                .collection("favorites")
                .get()
                .await()

            val categoryCount = mutableMapOf<String, Int>()

            for (document in snapshot.documents) {
                val productId = document.id
                try {
                    val productDoc = db.collection("products").document(productId).get().await()
                    val category = productDoc.getString("category") ?: "Otros"
                    categoryCount[category] = categoryCount.getOrDefault(category, 0) + 1
                } catch (e: Exception) {
                    categoryCount["Otros"] = categoryCount.getOrDefault("Otros", 0) + 1
                }
            }

            val predefinedCategories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val result = predefinedCategories.map { category ->
                category to categoryCount.getOrDefault(category, 0)
            }
            _favoritesData.postValue(result)
            */

            // DATOS FALSOS
            val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val counts = listOf(8, 12, 6, 4, 3, 2)
            _favoritesData.postValue(categories.zip(counts))

        } catch (e: Exception) {
            val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
            val counts = listOf(8, 12, 6, 4, 3, 2)
            _favoritesData.postValue(categories.zip(counts))
        }
    }

    // 6. Actividad de Usuarios (24h)
    private suspend fun loadUserFlowData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO) - Podrías usar Firebase Analytics aquí
            // Este es un ejemplo de cómo podrías obtener datos de actividad
            val snapshot = db.collection("user_activity")
                .whereGreaterThanOrEqualTo("timestamp", getLast24Hours())
                .get()
                .await()

            val hourlyActivity = mutableMapOf<String, Float>()

            snapshot.documents.forEach { document ->
                val timestamp = document.getTimestamp("timestamp")
                if (timestamp != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = timestamp.toDate()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
                    hourlyActivity[hour] = hourlyActivity.getOrDefault(hour, 0f) + 1f
                }
            }

            val hours = (0..23).map { it.toString() }
            val result = hours.map { hour ->
                hour to hourlyActivity.getOrDefault(hour, 0f)
            }
            _userFlowData.postValue(result)
            */

            // DATOS FALSOS - Patrón típico de uso de app
            val hours = (0..23).map { it.toString() }
            val activity = listOf(
                10f, 8f, 5f, 3f, 2f, 4f,        // Madrugada: poca actividad
                15f, 30f, 45f, 60f, 75f, 80f,   // Mañana: actividad creciente
                95f, 110f, 105f, 100f, 120f,    // Tarde: pico máximo
                150f, 140f, 120f, 90f, 60f, 30f // Noche: actividad decreciente
            )
            _userFlowData.postValue(hours.zip(activity))

        } catch (e: Exception) {
            val hours = (0..23).map { it.toString() }
            val activity = listOf(10f, 8f, 5f, 3f, 2f, 4f, 15f, 30f, 45f, 60f, 75f, 80f, 95f, 110f, 105f, 100f, 120f, 150f, 140f, 120f, 90f, 60f, 30f, 20f)
            _userFlowData.postValue(hours.zip(activity))
        }
    }

    // 7. Actividad Diaria
    private suspend fun loadDailyActivityData() {
        try {
            /*
            // CÓDIGO FIRESTORE (COMENTADO)
            val snapshot = db.collection("user_activity")
                .whereGreaterThanOrEqualTo("timestamp", getLast7Days())
                .get()
                .await()

            val dailyActivity = mutableMapOf<String, Float>()
            val calendar = Calendar.getInstance()

            snapshot.documents.forEach { document ->
                val timestamp = document.getTimestamp("timestamp")
                if (timestamp != null) {
                    calendar.time = timestamp.toDate()
                    val day = getDayName(calendar.get(Calendar.DAY_OF_WEEK))
                    dailyActivity[day] = dailyActivity.getOrDefault(day, 0f) + 1f
                }
            }

            val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            val result = days.map { day ->
                day to dailyActivity.getOrDefault(day, 0f)
            }
            _dailyActivityData.postValue(result)
            */

            // DATOS FALSOS - Patrón semanal típico
            val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            val activity = listOf(80f, 95f, 110f, 105f, 140f, 160f, 120f) // Fin de semana más actividad
            _dailyActivityData.postValue(days.zip(activity))

        } catch (e: Exception) {
            val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            val activity = listOf(80f, 95f, 110f, 105f, 140f, 160f, 120f)
            _dailyActivityData.postValue(days.zip(activity))
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            0 -> "Ene"; 1 -> "Feb"; 2 -> "Mar"; 3 -> "Abr"; 4 -> "May"; 5 -> "Jun"
            6 -> "Jul"; 7 -> "Ago"; 8 -> "Sep"; 9 -> "Oct"; 10 -> "Nov"; 11 -> "Dic"
            else -> ""
        }
    }

    private fun loadMockData() {
        // Cargar todos los datos falsos
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
        _salesColumnData.postValue(months.zip(listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)))

        val categories = listOf("Tecnología", "Libros", "Ropa", "Mobiliario", "Deportes", "Otros")
        _categoryData.postValue(categories.zip(listOf(4500f, 3200f, 1800f, 1200f, 800f, 600f)))

        _combinedChartData.postValue(months.zip(listOf(1200f, 1800f, 900f, 2200f, 1600f, 2500f)))
        _productStatusData.postValue(listOf("Activos" to 8f, "Vendidos" to 12f, "En Negociación" to 3f, "Reservados" to 2f))
        _favoritesData.postValue(categories.zip(listOf(8, 12, 6, 4, 3, 2)))

        val hours = (0..23).map { it.toString() }
        _userFlowData.postValue(hours.zip(listOf(10f, 8f, 5f, 3f, 2f, 4f, 15f, 30f, 45f, 60f, 75f, 80f, 95f, 110f, 105f, 100f, 120f, 150f, 140f, 120f, 90f, 60f, 30f, 20f)))

        val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        _dailyActivityData.postValue(days.zip(listOf(80f, 95f, 110f, 105f, 140f, 160f, 120f)))
    }

    fun refreshData() {
        loadDashboardData()
    }
}