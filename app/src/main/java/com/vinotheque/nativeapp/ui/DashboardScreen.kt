package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.DessertColor
import com.vinotheque.nativeapp.ui.theme.RedWineColor
import com.vinotheque.nativeapp.ui.theme.RoseWineColor
import com.vinotheque.nativeapp.ui.theme.SparklingColor
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R

@Composable
fun DashboardScreen(viewModel: WineViewModel) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val sales by viewModel.allSales.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val totalBottles = remember(wines) { wines.size }
    val totalValue = remember(wines) { wines.fold(0.0) { acc, w -> acc + w.price } }
    val totalSold = remember(sales) { sales.sumOf { it.quantity } }
    val salesRevenue = remember(sales) { sales.sumOf { it.price * it.quantity } }

    // Time-based stats from sales
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val weekMillis = 7 * dayMillis
    val monthMillis = 30 * dayMillis
    val yearMillis = 365 * dayMillis

    val todaySold = remember(sales) { sales.filter { now - it.timestamp < dayMillis }.sumOf { it.quantity } }
    val weekSold = remember(sales) { sales.filter { now - it.timestamp < weekMillis }.sumOf { it.quantity } }
    val monthSold = remember(sales) { sales.filter { now - it.timestamp < monthMillis }.sumOf { it.quantity } }
    val yearSold = remember(sales) { sales.filter { now - it.timestamp < yearMillis }.sumOf { it.quantity } }

    val userSales = remember(sales, currentUser) { sales.filter { it.username == currentUser }.sumOf { it.quantity } }
    val userRevenue = remember(sales, currentUser) { sales.filter { it.username == currentUser }.sumOf { it.price * it.quantity } }

    val redCount = remember(wines) { wines.count { it.type.equals("Red", ignoreCase = true) } }
    val whiteCount = remember(wines) { wines.count { it.type.equals("White", ignoreCase = true) } }
    val roseCount = remember(wines) { wines.count { it.type.replace("é", "e", ignoreCase = true).equals("Rose", ignoreCase = true) } }
    val sparkCount = remember(wines) { wines.count { it.type.equals("Sparkling", ignoreCase = true) } }
    val dessertCount = remember(wines) { wines.count { it.type.equals("Dessert", ignoreCase = true) } }
    val topWine = remember(wines) { wines.maxByOrNull { it.rating } }
    val mostExpensive = remember(wines) { wines.maxByOrNull { it.price } }
    val recentSales = remember(sales) { sales.take(10) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(bottom = 80.dp)
    ) {
        // Top Header with Greeting & Animated Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
                Text(
                    text = currentUser.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            AnimatedVinothequeLogo(modifier = Modifier.size(60.dp))
        }

        val lastBackup = viewModel.getLastBackupTime()
        if (lastBackup > 0) {
            val ago = (System.currentTimeMillis() - lastBackup) / 1000
            val timeText = when {
                ago < 60 -> "just now"
                ago < 3600 -> (ago / 60).toString() + "m ago"
                ago < 86400 -> (ago / 3600).toString() + "h ago"
                else -> (ago / 86400).toString() + "d ago"
            }
            Text("\u2601 Auto-saved $timeText", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.padding(start = 24.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it; viewModel.searchQuery.value = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            placeholder = { Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Time-based stat cards
            Text(stringResource(R.string.bottles_served), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeStatChip(stringResource(R.string.today), todaySold.toString(), Modifier.weight(1f))
                TimeStatChip(stringResource(R.string.week), weekSold.toString(), Modifier.weight(1f))
                TimeStatChip(stringResource(R.string.month), monthSold.toString(), Modifier.weight(1f))
                TimeStatChip(stringResource(R.string.year), yearSold.toString(), Modifier.weight(1f))
                TimeStatChip(stringResource(R.string.all_time), totalSold.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero stat cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroCard(stringResource(R.string.wines_stat), totalBottles.toString(), Icons.Default.LocalBar, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                HeroCard(stringResource(R.string.value_stat), "\u20AC" + totalValue.toInt().toString(), Icons.Default.TrendingUp, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroCard(stringResource(R.string.your_sales), userSales.toString(), Icons.Default.Star, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                HeroCard(stringResource(R.string.your_revenue), "\u20AC" + userRevenue.toInt().toString(), Icons.Default.TrendingUp, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Wine type distribution
            Text(stringResource(R.string.collection), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (totalBottles > 0) {
                TypeBar("Red", redCount, totalBottles, RedWineColor)
                Spacer(modifier = Modifier.height(8.dp))
                TypeBar("White", whiteCount, totalBottles, WhiteWineColor)
                Spacer(modifier = Modifier.height(8.dp))
                TypeBar("Rosé", roseCount, totalBottles, RoseWineColor)
                Spacer(modifier = Modifier.height(8.dp))
                TypeBar("Sparkling", sparkCount, totalBottles, SparklingColor)
                Spacer(modifier = Modifier.height(8.dp))
                TypeBar("Dessert", dessertCount, totalBottles, DessertColor)
            } else {
                Text("Add wines to see your collection breakdown", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
            }

            // Top Rated
            if (topWine != null) {
                Spacer(modifier = Modifier.height(28.dp))
                Text(stringResource(R.string.top_rated), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                HighlightCard(topWine.name, topWine.region, topWine.rating.toString() + "/100",
                    "\u20AC" + topWine.price.toInt().toString(), topWine.vintage)
            }
            if (mostExpensive != null && mostExpensive != topWine) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.most_valuable), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                HighlightCard(mostExpensive.name, mostExpensive.region, mostExpensive.rating.toString() + "/100",
                    "\u20AC" + mostExpensive.price.toInt().toString(), mostExpensive.vintage)
            }

            // Recent Activity Feed
            if (recentSales.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                Text(stringResource(R.string.recent_activity), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            for (sale in recentSales) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${sale.quantity}\u00D7 ${sale.wineName}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${sale.username} \u2022 ${sdf.format(Date(sale.timestamp))}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Text("\u20AC${(sale.price * sale.quantity).toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Serve Again quick action
                        TextButton(onClick = {
                            val wine = wines.firstOrNull { it.reference == sale.wineReference }
                            if (wine != null) viewModel.sellWine(wine)
                        }, modifier = Modifier.height(28.dp)) {
                            Icon(Icons.Default.Replay, "Serve Again", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun TimeStatChip(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
fun HeroCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
        }
    }
}

@Composable
fun TypeBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(24.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(count.toString(), color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp))
    }
}

@Composable
fun HighlightCard(name: String, region: String, rating: String, price: String, vintage: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(rating.substringBefore("/"), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2)
                Text(region + " | " + vintage, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Text(price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
