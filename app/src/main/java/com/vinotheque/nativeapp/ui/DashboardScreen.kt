package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import com.vinotheque.nativeapp.ui.theme.WineCard
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface

@Composable
fun DashboardScreen(viewModel: WineViewModel) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()

    val totalBottles = wines.size
    val totalValue = wines.fold(0.0) { acc, w -> acc + w.price * w.quantity }
    val avgRating = if (wines.isNotEmpty()) wines.sumOf { it.rating } / wines.size else 0
    val avgPrice = if (wines.isNotEmpty()) wines.fold(0.0) { acc, w -> acc + w.price } / wines.size else 0.0
    val totalStock = wines.sumOf { it.quantity }
    val totalSold = wines.sumOf { it.sold }
    val salesRevenue = wines.fold(0.0) { acc, w -> acc + w.price * w.sold }
    val redCount = wines.count { it.type.equals("Red", ignoreCase = true) }
    val whiteCount = wines.count { it.type.equals("White", ignoreCase = true) }
    val roseCount = wines.count { it.type.equals("Rose", ignoreCase = true) }
    val sparkCount = wines.count { it.type.equals("Sparkling", ignoreCase = true) }
    val dessertCount = wines.count { it.type.equals("Dessert", ignoreCase = true) }
    val topWine = wines.maxByOrNull { it.rating }
    val mostExpensive = wines.maxByOrNull { it.price }

    Column(
        modifier = Modifier.fillMaxSize().background(WineDark)
            .verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        // Header
        Text("Your Cellar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("at a glance", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(24.dp))

        // Hero stat cards
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard("Stock", totalStock.toString() + " btl", Icons.Default.LocalBar, WineGold, Modifier.weight(1f))
            HeroCard("Value", "\u20AC" + totalValue.toInt().toString(), Icons.Default.TrendingUp, WineRed, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard("Sold", totalSold.toString() + " btl", Icons.Default.Star, WineGold, Modifier.weight(1f))
            HeroCard("Revenue", "\u20AC" + salesRevenue.toInt().toString(), Icons.Default.TrendingUp, WineGoldDim, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard("Wines", totalBottles.toString(), Icons.Default.LocalBar, WineGoldDim, Modifier.weight(1f))
            HeroCard("Avg Price", "\u20AC" + avgPrice.toInt().toString(), Icons.Default.TrendingUp, WineGoldDim, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Wine type distribution
        Text("Collection", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalBottles > 0) {
                TypeBar("Red", redCount, totalBottles, RedWineColor)
                TypeBar("White", whiteCount, totalBottles, WhiteWineColor)
                TypeBar("Rose", roseCount, totalBottles, RoseWineColor)
                TypeBar("Sparkling", sparkCount, totalBottles, SparklingColor)
                TypeBar("Dessert", dessertCount, totalBottles, DessertColor)
            } else {
                Text("Add wines to see your collection breakdown", color = TextTertiary, fontSize = 14.sp)
            }
        }

        // Top Rated
        if (topWine != null) {
            Spacer(modifier = Modifier.height(28.dp))
            Text("Top Rated", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            HighlightCard(topWine.name, topWine.region, topWine.rating.toString() + "/100",
                "\u20AC" + topWine.price.toInt().toString(), topWine.vintage)
        }
        if (mostExpensive != null && mostExpensive != topWine) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Most Valuable", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            HighlightCard(mostExpensive.name, mostExpensive.region, mostExpensive.rating.toString() + "/100",
                "\u20AC" + mostExpensive.price.toInt().toString(), mostExpensive.vintage)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun HeroCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WineSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
fun TypeBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    val percent = (fraction * 100).toInt()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(12.dp)).background(WineSurface)) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(24.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(count.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(30.dp))
    }
}

@Composable
fun HighlightCard(name: String, region: String, rating: String, price: String, vintage: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Rating badge
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))),
                contentAlignment = Alignment.Center
            ) {
                Text(rating.substringBefore("/"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2)
                Text(region + " | " + vintage, color = TextSecondary, fontSize = 12.sp)
            }
            Text(price, color = WineGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
