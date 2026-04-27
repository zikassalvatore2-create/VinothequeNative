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
import com.vinotheque.nativeapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesAnalyticsScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val sales by viewModel.allSales.collectAsState()
    
    val totalRevenue = sales.sumOf { it.price }
    val totalCount = sales.size
    
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val weekMillis = 7 * dayMillis
    val monthMillis = 30 * dayMillis
    val yearMillis = 365 * dayMillis
    
    val dailySales = sales.filter { now - it.timestamp < dayMillis }
    val weeklySales = sales.filter { now - it.timestamp < weekMillis }
    val monthlySales = sales.filter { now - it.timestamp < monthMillis }
    val yearlySales = sales.filter { now - it.timestamp < yearMillis }
    
    val userSales = sales.groupBy { it.username }.mapValues { entry ->
        entry.value.size to entry.value.sumOf { it.price }
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Text("Sales Analytics", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                AnalyticsCard("Overall Summary") {
                    StatRow("Total Sales", totalCount.toString())
                    StatRow("Total Revenue", "€${totalRevenue.toInt()}")
                }
            }
            
            item {
                AnalyticsCard("Time Periods") {
                    StatRow("Today", "€${dailySales.sumOf { it.price }.toInt()} (${dailySales.size})")
                    StatRow("This Week", "€${weeklySales.sumOf { it.price }.toInt()} (${weeklySales.size})")
                    StatRow("This Month", "€${monthlySales.sumOf { it.price }.toInt()} (${monthlySales.size})")
                    StatRow("This Year", "€${yearlySales.sumOf { it.price }.toInt()} (${yearlySales.size})")
                }
            }
            
            item {
                AnalyticsCard("Sales by User") {
                    userSales.forEach { (user, stats) ->
                        StatRow(user, "€${stats.second.toInt()} (${stats.first})")
                    }
                }
            }
            
            item {
                Text("Recent Transactions", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
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
        colors = CardDefaults.cardColors(containerColor = WineSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = WineGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = TextTertiary.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SaleItem(sale: Sale) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WineSurface.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TrendingUp, null, tint = WineGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sale.wineName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("by ${sale.username} • ${sdf.format(Date(sale.timestamp))}", color = TextTertiary, fontSize = 11.sp)
            }
            Text("€${sale.price.toInt()}", color = WineGold, fontWeight = FontWeight.Bold)
        }
    }
}
