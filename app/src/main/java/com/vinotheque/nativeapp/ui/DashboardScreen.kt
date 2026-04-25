package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val CardBg = Color(0xFF1A0A0A)

@Composable
fun DashboardScreen(viewModel: WineViewModel) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()

    val totalBottles = wines.size
    val totalValue = wines.fold(0.0) { acc, w -> acc + w.price }
    val avgRating = if (wines.isNotEmpty()) wines.sumOf { it.rating } / wines.size else 0
    val redCount = wines.count { it.type.equals("Red", ignoreCase = true) }
    val whiteCount = wines.count { it.type.equals("White", ignoreCase = true) }
    val roseCount = wines.count { it.type.equals("Rose", ignoreCase = true) }
    val sparkCount = wines.count { it.type.equals("Sparkling", ignoreCase = true) }
    val dessertCount = wines.count { it.type.equals("Dessert", ignoreCase = true) }
    val topWine = wines.maxByOrNull { it.rating }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Cellar Insights", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Stats row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InsightCard("Bottles", totalBottles.toString(), Modifier.weight(1f))
            InsightCard("Value", "E" + totalValue.toInt().toString(), Modifier.weight(1f))
            InsightCard("Avg Rating", avgRating.toString(), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Wine Types", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip("Red", redCount, Modifier.weight(1f))
            TypeChip("White", whiteCount, Modifier.weight(1f))
            TypeChip("Rose", roseCount, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip("Sparkling", sparkCount, Modifier.weight(1f))
            TypeChip("Dessert", dessertCount, Modifier.weight(1f))
        }

        if (topWine != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Top Rated Wine", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(topWine.name, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(topWine.region, color = Color.Gray, fontSize = 14.sp)
                    HorizontalDivider(color = Gold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(topWine.rating.toString() + "/100", color = Gold, fontWeight = FontWeight.Bold)
                        Text("E" + topWine.price.toInt().toString(), color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun InsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Gold, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TypeChip(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(count.toString(), color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
