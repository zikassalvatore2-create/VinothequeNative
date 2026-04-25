package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface

@Composable
fun PairingScreen(viewModel: WineViewModel, onWineClick: (Wine) -> Unit) {
    var selectedDish by remember { mutableStateOf("") }
    val matchingWines = if (selectedDish.isNotEmpty()) viewModel.getWinesByPairing(selectedDish) else emptyList()

    Column(modifier = Modifier.fillMaxSize().background(WineDark).padding(20.dp)) {
        Text("Wine Pairing", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Find the perfect match", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))

        // Dish grid
        val dishes = listOf("Beef", "Lamb", "Pork", "Poultry", "Seafood", "Pasta", "Cheese", "Dessert")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dishes.take(4).forEach { dish ->
                Button(onClick = { selectedDish = dish },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDish == dish) WineGold else WineSurface),
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)
                ) { Text(dish, color = if (selectedDish == dish) Color.Black else TextSecondary,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dishes.drop(4).forEach { dish ->
                Button(onClick = { selectedDish = dish },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDish == dish) WineGold else WineSurface),
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)
                ) { Text(dish, color = if (selectedDish == dish) Color.Black else TextSecondary,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedDish.isNotEmpty()) {
            Text(matchingWines.size.toString() + " wines for " + selectedDish,
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (matchingWines.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Restaurant, "No match", tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matching wines", color = TextSecondary, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(matchingWines) { wine ->
                        Card(onClick = { onWineClick(wine) }, shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WineSurface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))),
                                        contentAlignment = Alignment.Center) {
                                        Text(wine.rating.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wine.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                                        Text(wine.region + " | " + wine.grape, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Text("E" + wine.price.toInt().toString(), color = WineGold, fontWeight = FontWeight.Bold)
                                }
                                if (wine.foodPairing.isNotEmpty()) {
                                    HorizontalDivider(color = WineSurface, modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Pairs with: " + wine.foodPairing, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
