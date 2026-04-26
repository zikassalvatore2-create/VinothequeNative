package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.DessertColor
import com.vinotheque.nativeapp.ui.theme.RedWineColor
import com.vinotheque.nativeapp.ui.theme.RoseWineColor
import com.vinotheque.nativeapp.ui.theme.SparklingColor
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineSurface

fun getTypeColor(type: String): Color = when (type.lowercase()) {
    "red" -> RedWineColor
    "white" -> WhiteWineColor
    "rose" -> RoseWineColor
    "sparkling" -> SparklingColor
    "dessert" -> DessertColor
    else -> WineGold
}

@Composable
fun CellarScreen(viewModel: WineViewModel, onWineClick: (Wine) -> Unit, onDeleteWine: (Wine) -> Unit) {
    val wines by viewModel.wines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    var wineToDelete by remember { mutableStateOf<Wine?>(null) }

    if (wineToDelete != null) {
        AlertDialog(onDismissRequest = { wineToDelete = null },
            title = { Text("Remove Wine") },
            text = { Text("Remove " + (wineToDelete?.name ?: "") + " from your cellar?") },
            confirmButton = { TextButton(onClick = { wineToDelete?.let { onDeleteWine(it) }; wineToDelete = null }) {
                Text("Remove", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { wineToDelete = null }) { Text("Cancel") } })
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)) {
        // Search bar with subtle glow
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search wines, bins, regions...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = WineGold.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WineGold.copy(alpha = 0.5f),
                unfocusedBorderColor = WineSurface,
                focusedContainerColor = WineSurface,
                unfocusedContainerColor = WineSurface,
                cursorColor = WineGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("" to "All", "Red" to "Red", "White" to "White", "Rose" to "Rosé",
                "Sparkling" to "Spark", "Dessert" to "Sweet").forEach { (value, label) ->
                val isSelected = typeFilter == value
                Button(
                    onClick = { viewModel.typeFilter.value = value },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) WineGold else WineSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(label, color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results
        Text(wines.size.toString() + " wines", color = TextTertiary, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))

        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalBar, "Empty", tint = WineGold.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cellar is empty", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add wines or load sample data", color = TextTertiary, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wines, key = { it.reference }) { wine ->
                    WineCard(wine, onClick = { onWineClick(wine) }, onLongClick = { wineToDelete = wine })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineCard(wine: Wine, onClick: () -> Unit, onLongClick: () -> Unit) {
    // Use global LRU cache instead of per-recomposition decoding
    val decodedBitmap: ImageBitmap? = remember(wine.image) { BitmapCache.get(wine.image) }
    val typeColor = getTypeColor(wine.type)

    // Fade-in animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(400), label = "cardAlpha")
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.62f).alpha(alpha)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WineSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image — elegant gradient background
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .background(Brush.verticalGradient(listOf(
                            WineSurface,
                            typeColor.copy(alpha = 0.05f),
                            WineSurface
                        ))),
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(bitmap = decodedBitmap, contentDescription = wine.name,
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit)
                    } else {
                        // Elegant placeholder with type-tinted glass
                        Icon(Icons.Default.LocalBar, wine.name,
                            tint = typeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp))
                    }
                }
                // Info section
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(wine.name, color = Color.White, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, maxLines = 2, lineHeight = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(wine.region, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                    if (wine.binLocation.isNotEmpty()) {
                        Text("📍 " + wine.binLocation, color = WineGold.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("\u20AC" + wine.price.toInt().toString(), color = WineGold,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(wine.vintage, color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
            // Rating badge — elegant gold circle
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .size(34.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))),
                contentAlignment = Alignment.Center
            ) {
                Text(wine.rating.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            // Type badge — frosted glass effect
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(wine.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Sold badge
            if (wine.sold > 0) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WineGold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(wine.sold.toString() + " sold", color = WineGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
