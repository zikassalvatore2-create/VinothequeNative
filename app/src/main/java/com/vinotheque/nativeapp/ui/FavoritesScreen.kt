package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vinotheque.nativeapp.data.Wine

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)

@Composable
fun FavoritesScreen(viewModel: WineViewModel, onWineClick: (Wine) -> Unit) {
    val favorites by viewModel.favoriteWines.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)) {
        Text("Favorites", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No favorites yet", color = Color.Gray, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap the heart on any wine to add it", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(favorites) { wine ->
                    FavoriteRow(wine = wine, onClick = { onWineClick(wine) })
                }
            }
        }
    }
}

@Composable
fun FavoriteRow(wine: Wine, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
            .background(Color.Transparent),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1A0A0A))
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(wine.name, color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(wine.region + " | " + wine.vintage, color = Color.Gray, fontSize = 12.sp)
                Text(wine.grape, color = Color.LightGray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(wine.rating.toString() + "pt", color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("E" + wine.price.toInt().toString(), color = Gold, fontSize = 14.sp)
            }
        }
    }
}
