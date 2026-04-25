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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val CardBg = Color(0xFF1A0A0A)

@Composable
fun PairingScreen(viewModel: WineViewModel, onWineClick: (Wine) -> Unit) {
    var selectedDish by remember { mutableStateOf("") }
    val matchingWines = if (selectedDish.isNotEmpty()) viewModel.getWinesByPairing(selectedDish) else emptyList()

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)) {
        Text("Wine Pairing", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Select a dish to find matching wines", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Dish categories
        val dishes = listOf("Beef", "Lamb", "Pork", "Poultry", "Seafood", "Pasta", "Cheese", "Dessert")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dishes.take(4).forEach { dish ->
                Button(
                    onClick = { selectedDish = dish },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDish == dish) Gold else CardBg),
                    modifier = Modifier.weight(1f)
                ) { Text(dish, color = if (selectedDish == dish) Color.Black else Color.White, fontSize = 11.sp) }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dishes.drop(4).forEach { dish ->
                Button(
                    onClick = { selectedDish = dish },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDish == dish) Gold else CardBg),
                    modifier = Modifier.weight(1f)
                ) { Text(dish, color = if (selectedDish == dish) Color.Black else Color.White, fontSize = 11.sp) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedDish.isNotEmpty()) {
            Text(
                matchingWines.size.toString() + " wines for " + selectedDish,
                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (matchingWines.isEmpty()) {
                Text("No matching wines in your cellar. Try adding more wines!", color = Color.Gray, fontSize = 14.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(matchingWines) { wine ->
                        PairingCard(wine = wine, onClick = { onWineClick(wine) })
                    }
                }
            }
        }
    }
}

@Composable
fun PairingCard(wine: Wine, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(wine.name, color = Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(wine.rating.toString() + "pt", color = Gold, fontWeight = FontWeight.Bold)
            }
            Text(wine.region + " | " + wine.grape, color = Color.Gray, fontSize = 12.sp)
            if (wine.foodPairing.isNotEmpty()) {
                HorizontalDivider(color = Gold.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                Text("Pairs with: " + wine.foodPairing, color = Color.LightGray, fontSize = 13.sp)
            }
            if (wine.aroma.isNotEmpty()) {
                Text("Aroma: " + wine.aroma, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
