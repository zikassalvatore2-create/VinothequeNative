package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(viewModel: WineViewModel) {
    val wines by viewModel.wines.collectAsState()
    
    val totalBottles = wines.size
    val totalValue = wines.sumOf { it.price }
    val redCount = wines.count { it.type.equals("Red", ignoreCase = true) }
    val whiteCount = wines.count { it.type.equals("White", ignoreCase = true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0d0505))
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar()
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cellar Insights", color = Color(0xFFd4a54e), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(title = "Total Bottles", value = totalBottles.toString(), modifier = Modifier.weight(1f))
                StatCard(title = "Cellar Value", value = "€${totalValue.toInt()}", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Distribution
            Text("Wine Types", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            StatCard(title = "Reds", value = redCount.toString(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            StatCard(title = "Whites", value = whiteCount.toString(), modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(1.dp, Color(0xFFd4a54e).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x332d1212))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color(0xFFd4a54e), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}
