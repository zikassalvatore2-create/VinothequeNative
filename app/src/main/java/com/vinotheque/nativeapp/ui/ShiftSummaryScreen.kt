package com.vinotheque.nativeapp.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShiftSummaryScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val sales by viewModel.allSales.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Default shift = Dinner (17:00 to now, or last 8 hours)
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 17)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    if (cal.timeInMillis > now) cal.add(Calendar.DAY_OF_MONTH, -1)
    val shiftStart = cal.timeInMillis

    var shiftLabel by remember { mutableStateOf("Dinner") }
    val shiftSales = remember(sales, shiftStart) {
        sales.filter { it.timestamp >= shiftStart && it.username == currentUser }
    }
    val totalBottles = remember(shiftSales) { shiftSales.sumOf { it.quantity } }
    val totalValue = remember(shiftSales) { shiftSales.sumOf { it.price * it.quantity } }
    val topWines = remember(shiftSales) {
        shiftSales.groupBy { it.wineName }.mapValues { it.value.sumOf { s -> s.quantity } }
            .entries.sortedByDescending { it.value }.take(5)
    }
    val avgRating = remember(shiftSales) {
        val wines = viewModel.allWinesUnfiltered.value
        val refs = shiftSales.map { it.wineReference }.toSet()
        val matched = wines.filter { it.reference in refs }
        if (matched.isNotEmpty()) matched.sumOf { it.rating } / matched.size else 0
    }

    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

    fun buildShareText(): String {
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════")
        sb.appendLine("   VINOTHEQUE PRO")
        sb.appendLine("   Shift Summary")
        sb.appendLine("═══════════════════════")
        sb.appendLine()
        sb.appendLine("Date: ${dateFormat.format(Date(now))}")
        sb.appendLine("Shift: $shiftLabel")
        sb.appendLine("Server: $currentUser")
        sb.appendLine()
        sb.appendLine("Total Bottles: $totalBottles")
        sb.appendLine("Total Value: €${totalValue.toInt()}")
        sb.appendLine("Avg Rating: $avgRating/100")
        sb.appendLine()
        if (topWines.isNotEmpty()) {
            sb.appendLine("Top 5 Wines:")
            topWines.forEachIndexed { i, entry ->
                sb.appendLine("  ${i + 1}. ${entry.key} (${entry.value}×)")
            }
        }
        sb.appendLine()
        sb.appendLine("═══════════════════════")
        return sb.toString()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text("Shift Summary", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, buildShareText())
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Shift Summary"))
            }) { Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary) }
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // Date & Shift label
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(dateFormat.format(Date(now)), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Lunch", "Dinner", "Custom").forEach { label ->
                            FilterChip(
                                selected = shiftLabel == label,
                                onClick = { shiftLabel = label },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                    }
                    Text("Server: $currentUser", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Bottles", totalBottles.toString(), Modifier.weight(1f))
                StatCard("Revenue", "€${totalValue.toInt()}", Modifier.weight(1f))
                StatCard("Avg Rating", avgRating.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top wines
            if (topWines.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top Wines This Shift", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        topWines.forEachIndexed { i, entry ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${i + 1}. ${entry.key}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1)
                                Text("${entry.value}×", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text("No sales this shift yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp,
                        modifier = Modifier.padding(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share button
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildShareText())
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Shift Summary"))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Summary", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}
