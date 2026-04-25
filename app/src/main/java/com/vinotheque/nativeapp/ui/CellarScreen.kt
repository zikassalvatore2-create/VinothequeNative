package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine

@Composable
fun CellarScreen(viewModel: WineViewModel) {
    val wines by viewModel.wines.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar()
        
        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { viewModel.addSampleWine() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
                ) {
                    Text("Add Sample Wine", color = Color.Black)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(wines) { wine ->
                    WineCard(wine)
                }
            }
        }
    }
}

@Composable
fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1a0a0a))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🍷 Vinothèque Pro", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WineCard(wine: Wine) {
    val goldBorder = Brush.linearGradient(listOf(Color(0xFFd4a54e), Color(0xFF9a7b3a)))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .border(1.dp, goldBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x332d1212))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(wine.name, color = Color(0xFFd4a54e), fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2)
            Text(wine.region, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(wine.vintage, color = Color.Gray, fontSize = 12.sp)
                Text(wine.type, color = Color.Gray, fontSize = 12.sp)
            }
            
            Divider(color = Color(0xFFd4a54e).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("€${wine.price.toInt()}", color = Color(0xFFd4a54e), fontWeight = FontWeight.Bold)
                Text("★ ${wine.rating}", color = Color(0xFFd4a54e))
            }
        }
    }
}
