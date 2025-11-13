package com.atom.unimarket.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.atom.unimarket.presentation.dashboard.DashboardViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun")
    val values = listOf(20f, 35f, 28f, 42f, 30f, 38f)

    val chartModel = entryModelOf(
        0f to 20f,
        1f to 35f,
        2f to 28f,
        3f to 42f,
        4f to 30f,
        5f to 38f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard de Ventas",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        Chart(
            chart = columnChart(),
            model = chartModel,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { x, _ -> months.getOrNull(x.toInt()) ?: "" }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}