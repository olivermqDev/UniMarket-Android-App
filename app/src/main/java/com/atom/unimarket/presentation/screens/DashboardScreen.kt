package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.atom.unimarket.presentation.dashboard.DashboardViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import com.patrykandpatrick.vico.compose.chart.line.lineChart


@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    val salesColumnData by dashboardViewModel.salesColumnData.observeAsState()
    val categoryData by dashboardViewModel.categoryData.observeAsState()
    val combinedChartData by dashboardViewModel.combinedChartData.observeAsState()
    val productStatusData by dashboardViewModel.productStatusData.observeAsState()
    val favoritesData by dashboardViewModel.favoritesData.observeAsState()
    val userFlowData by dashboardViewModel.userFlowData.observeAsState()
    val dailyActivityData by dashboardViewModel.dailyActivityData.observeAsState()
    val isLoading by dashboardViewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboardData()
    }

    if (isLoading && salesColumnData == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando datos del dashboard...")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi Dashboard UniMarket",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { dashboardViewModel.refreshData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Ventas Mensuales
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💰 Ventas Mensuales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!salesColumnData.isNullOrEmpty()) {
                        SalesColumnChart(salesColumnData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                            Text("No hay datos de ventas")
                        }
                    }
                }
            }

            // 2. Ventas por Categoría
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏷️ Ventas por Categoría",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!categoryData.isNullOrEmpty()) {
                        CategoryChart(categoryData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No hay datos de categorías")
                        }
                    }
                }
            }

            // 3. Ventas vs Promedio Móvil
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Ventas vs Promedio Móvil",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!combinedChartData.isNullOrEmpty()) {
                        FakeCombinedChart(combinedChartData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                            Text("No hay datos combinados")
                        }
                    }
                }
            }

            // 4. Estado de Productos
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📦 Estado de Productos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!productStatusData.isNullOrEmpty()) {
                        ProductStatusChart(productStatusData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            Text("Sin datos de productos")
                        }
                    }
                }
            }

            // 5. Tus Favoritos
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "❤️ Tus Favoritos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!favoritesData.isNullOrEmpty()) {
                        FavoritesStats(favoritesData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("❤️", style = MaterialTheme.typography.headlineMedium)
                                Text("No hay favoritos", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 6. Actividad de Usuarios (24h)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👥 Actividad de Usuarios (24h)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!userFlowData.isNullOrEmpty()) {
                        FakeUserFlowAreaChart(userFlowData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                            Text("No hay datos de actividad")
                        }
                    }
                }
            }

            // 7. Actividad Diaria
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📱 Actividad Diaria",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!dailyActivityData.isNullOrEmpty()) {
                        DailyActivityLineChart(dailyActivityData!!)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                            Text("No hay datos de actividad")
                        }
                    }
                }
            }
        }
    }
}

//Prueba 1
@Composable
fun SalesColumnChart(salesData: List<Pair<String, Float>>) {
    val months = salesData.map { it.first }
    val values = salesData.map { it.second }

    val chartModel = entryModelOf(
        *values.mapIndexed { index, value -> index.toFloat() to value }.toTypedArray()
    )

    Chart(
        chart = columnChart(),
        model = chartModel,
        startAxis = rememberStartAxis(
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode() // CORRECCIÓN
            }
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, _ -> months.getOrNull(x.toInt()) ?: "" },
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode() // CORRECCIÓN
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

//Prueba 2:
@Composable
fun DailyActivityLineChart(dailyData: List<Pair<String, Float>>) {
    val days = dailyData.map { it.first }
    val values = dailyData.map { it.second }

    val chartModel = entryModelOf(
        *values.mapIndexed { index, value -> index.toFloat() to value }.toTypedArray()
    )

    Chart(
        chart = lineChart(),
        model = chartModel,
        startAxis = rememberStartAxis(
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode() // CORRECCIÓN
            }
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, _ -> days.getOrNull(x.toInt()) ?: "" },
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode() // CORRECCIÓN
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

//Prueba 3 :
@Composable
fun FakeUserFlowAreaChart(userFlowData: List<Pair<String, Float>>) {
    val hours = (0..23).map { it.toString() }
    val values = listOf(
        10f, 15f, 20f, 30f, 50f, 80f, 120f, 150f, 180f, 200f, 210f, 220f,
        250f, 260f, 240f, 220f, 200f, 170f, 140f, 100f, 80f, 50f, 30f, 20f
    )

    val model = entryModelOf(
        *values.mapIndexed { i, v -> i.toFloat() to v }.toTypedArray()
    )

    // Obtener los colores fuera del Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryWithAlpha = primaryColor.copy(alpha = 0.18f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
    ) {
        Chart(
            chart = com.patrykandpatrick.vico.compose.chart.line.lineChart(),
            model = model,
            startAxis = rememberStartAxis(
                label = textComponent {
                    color = MaterialTheme.colorScheme.onSurface.hashCode()
                }
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { x, _ -> hours.getOrNull(x.toInt()) ?: "" },
                label = textComponent {
                    color = MaterialTheme.colorScheme.onSurface.hashCode()
                }
            ),
            modifier = Modifier.matchParentSize()
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            if (values.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val count = values.size
            val maxV = (values.maxOrNull() ?: 1f)

            val points = values.mapIndexed { i, v ->
                val x = i * (w / (count - 1).coerceAtLeast(1))
                val y = h - (v / maxV) * h
                Offset(x, y)
            }

            // draw filled polygon (area)
            val path = androidx.compose.ui.graphics.Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, h) // bottom-left
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, h) // bottom-right
                    close()
                }
            }
            drawPath(path, color = primaryWithAlpha)

            // draw line on top
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = primaryColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

//Prueba 4 :
@Composable
fun FakeCombinedChart(combinedChartData: List<Pair<String, Float>>) {
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
    val sales = listOf(100f, 180f, 90f, 220f, 150f, 260f)

    // Simple average line
    val avg = sales.mapIndexed { i, _ ->
        val window = listOf(i - 1, i, i + 1).mapNotNull { idx -> sales.getOrNull(idx) }
        window.average().toFloat()
    }

    // Column model
    val model = entryModelOf(*sales.mapIndexed { i, v -> i.toFloat() to v }.toTypedArray())

    // Obtener colores fuera del Canvas
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
    ) {
        Chart(
            chart = columnChart(),
            model = model,
            startAxis = rememberStartAxis(
                label = textComponent {
                    color = MaterialTheme.colorScheme.onSurface.hashCode()
                }
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { x, _ -> months.getOrNull(x.toInt()) ?: "" },
                label = textComponent {
                    color = MaterialTheme.colorScheme.onSurface.hashCode()
                }
            ),
            modifier = Modifier.matchParentSize()
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            if (sales.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val count = sales.size
            val maxV = (sales + avg).maxOrNull() ?: 1f

            val linePoints = avg.mapIndexed { i, v ->
                val x = i * (w / (count - 1).coerceAtLeast(1))
                val y = h - (v / maxV) * h
                Offset(x, y)
            }

            for (i in 0 until linePoints.size - 1) {
                drawLine(
                    color = secondaryColor,
                    start = linePoints[i],
                    end = linePoints[i + 1],
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }

            // draw small circles on points
            linePoints.forEach {
                drawCircle(
                    color = secondaryColor,
                    radius = 6f,
                    center = it
                )
            }
        }
    }
}

@Composable
fun FavoritesStats(favoritesData: List<Pair<String, Int>>) {
    val filteredData = favoritesData.filter { it.second > 0 }
    val totalFavorites = filteredData.sumOf { it.second }

    if (filteredData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❤️", style = MaterialTheme.typography.headlineMedium)
                Text("No hay favoritos", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Total de favoritos - Más destacado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total de Favoritos",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Productos que te han gustado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$totalFavorites ❤️",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Categoría más popular
        val topCategory = filteredData.maxByOrNull { it.second }
        topCategory?.let { (category, count) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Categoría Favorita",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "La que más te gusta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "$count productos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Distribución por categorías - Con mejor formato
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Distribución por Categorías",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                filteredData.forEach { (category, count) ->
                    val percentage = (count.toFloat() / totalFavorites * 100)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${"%.0f".format(percentage)}% ($count)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (category != filteredData.last().first) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SalesChart(salesData: List<Pair<String, Float>>) {
    val months = salesData.map { it.first }
    val values = salesData.map { it.second }

    val chartModel = entryModelOf(
        *values.mapIndexed { index, value -> index.toFloat() to value }.toTypedArray()
    )

    Chart(
        chart = columnChart(),
        model = chartModel,
        startAxis = rememberStartAxis(
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode()
            }
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, _ -> months.getOrNull(x.toInt()) ?: "" },
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun CategoryChart(categoryData: List<Pair<String, Float>>) {
    val categories = categoryData.map { it.first }
    val values = categoryData.map { it.second }

    val chartModel = entryModelOf(
        *values.mapIndexed { index, value -> index.toFloat() to value }.toTypedArray()
    )

    Chart(
        chart = columnChart(),
        model = chartModel,
        startAxis = rememberStartAxis(
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode()
            }
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, _ -> categories.getOrNull(x.toInt()) ?: "" },
            label = textComponent {
                color = MaterialTheme.colorScheme.onSurface.hashCode()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun ProductStatusChart(productStatusData: List<Pair<String, Float>>) {
    val total = productStatusData.sumOf { it.second.toDouble() }.toFloat()
    val statusColors = listOf(
        MaterialTheme.colorScheme.primary to "🟢", // Activos
        MaterialTheme.colorScheme.secondary to "💰", // Vendidos
        MaterialTheme.colorScheme.tertiary to "🟡", // En Negociación
        Color(0xFF9C27B0) to "🟣" // Reservados
    )

    Column {
        productStatusData.forEachIndexed { index, (status, count) ->
            val percentage = if (total > 0) (count / total * 100) else 0f
            val (color, emoji) = statusColors.getOrNull(index) ?: (MaterialTheme.colorScheme.onSurface to "⚪")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = status,
                        color = color,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${count.toInt()} (${"%.1f".format(percentage)}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (index < productStatusData.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
    }
}