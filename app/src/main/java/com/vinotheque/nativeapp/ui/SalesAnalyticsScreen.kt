package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Sale
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesAnalyticsScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val sales by viewModel.allSales.collectAsState()
    
    val totalRevenue = remember(sales) { sales.sumOf { it.price * it.quantity } }
    val totalCount = remember(sales) { sales.sumOf { it.quantity } }
    
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val weekMillis = 7 * dayMillis
    val monthMillis = 30 * dayMillis
    val yearMillis = 365 * dayMillis
    
    val dailySales = remember(sales) { sales.filter { now - it.timestamp < dayMillis } }
    val weeklySales = remember(sales) { sales.filter { now - it.timestamp < weekMillis } }
    val monthlySales = remember(sales) { sales.filter { now - it.timestamp < monthMillis } }
    val yearlySales = remember(sales) { sales.filter { now - it.timestamp < yearMillis } }
    
    val userSales = remember(sales) { sales.groupBy { it.username }.mapValues { entry ->
        entry.value.sumOf { it.quantity } to entry.value.sumOf { it.price * it.quantity }
    }}

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Sales Analytics", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                AnalyticsCard("Overall Summary") {
                    StatRow("Total Bottles", totalCount.toString())
                    StatRow("Total Revenue", "\u20AC${totalRevenue.toInt()}")
                }
            }
            
            item {
                AnalyticsCard("Time Periods") {
                    StatRow("Today", "\u20AC${dailySales.sumOf { it.price * it.quantity }.toInt()} (${dailySales.sumOf { it.quantity }} btl)")
                    StatRow("This Week", "\u20AC${weeklySales.sumOf { it.price * it.quantity }.toInt()} (${weeklySales.sumOf { it.quantity }} btl)")
                    StatRow("This Month", "\u20AC${monthlySales.sumOf { it.price * it.quantity }.toInt()} (${monthlySales.sumOf { it.quantity }} btl)")
                    StatRow("This Year", "\u20AC${yearlySales.sumOf { it.price * it.quantity }.toInt()} (${yearlySales.sumOf { it.quantity }} btl)")
                }
            }
            
            item {
                AnalyticsCard("Sales by User") {
                    userSales.forEach { (user, stats) ->
                        StatRow(user, "\u20AC${stats.second.toInt()} (${stats.first} btl)")
                    }
                }
            }
            
            item {
                Text("Recent Transactions", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            }
            
            items(sales.take(50)) { sale ->
                SaleItem(sale)
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SaleItem(sale: Sale) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${sale.quantity}\u00D7 ${sale.wineName}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("by ${sale.username} \u2022 ${sdf.format(Date(sale.timestamp))}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
            }
            Text("\u20AC${(sale.price * sale.quantity).toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
